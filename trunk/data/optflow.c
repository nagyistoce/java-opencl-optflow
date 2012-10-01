#define GLOBAL_G

const sampler_t sam_int=CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;
const sampler_t sam_norm=CLK_NORMALIZED_COORDS_TRUE  | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_LINEAR;
const sampler_t sam_linear= CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_LINEAR;

float lumii(float3 v){
	float3 k=(float3)(0.3f,0.59f,0.11f);
	return dot(v,k);
}
float lumi(image2d_t srci,int2 coords){
	float4 col=read_imagef(srci, sam_int, coords);
	return lumii(col.xyz);
}
__kernel void preprocess(read_only image2d_t src, write_only image2d_t dst) {
	int2 cds = (int2)(get_global_id(0), get_global_id(1));
	float v=lumi(src,cds);
	float ix=lumi(src,cds+(int2)(1,0))-lumi(src,cds+(int2)(-1,0));
	float iy=lumi(src,cds+(int2)(0,1))-lumi(src,cds+(int2)(0,-1));
	write_imagef(dst, cds, (float4)(ix,iy,v,1) );
}
__constant int size=5;

//calc current flow
__kernel void solve(
		read_only image2d_t src1,
		read_only image2d_t diff,
		read_only image2d_t src_g,
		write_only image2d_t dst_g
		) {
	int2 cds = (int2)(get_global_id(0), get_global_id(1));
	float2 cdsf=convert_float2(cds);
	float2 base =1.0f/(float2)(get_global_size(0)-1,get_global_size(1)-1);
	float2 g=read_imagef(src_g, sam_norm, cdsf * base).xy*2000.0f;

	float xx=0,xy=0,yy=0;
	for(float x=-size;x<=size;x+=1){
		for(float y=-size;y<=size;y+=1){
			float2 pt=cdsf+(float2)(x,y);
			float4 s0=read_imagef(diff, sam_linear, pt);
			xx+=s0.x*s0.x;
			xy+=s0.x*s0.y;
			yy+=s0.y*s0.y;
		}
	}

	float2 bk=0;
	for(float x=-size;x<=size;x+=1){
		for(float y=-size;y<=size;y+=1){
			float2 pt=cdsf+(float2)(x,y);
#ifdef GLOBAL_G
				float2 gl=g;
#else
				float2 gl=read_imagef(src_g, sam_linear, pt*base).xy*1000.0f;
#endif
			float ep=read_imagef(diff, sam_linear, pt).z - lumii(read_imagef(src1, sam_linear, pt + gl).xyz);
			bk +=ep * read_imagef(diff, sam_linear, pt).xy;
		}
	}
	float t=xx*yy-xy*xy;
	if(t==0){
		t=1e-10;
	}
	float2 li= (float2)(yy/(t)*bk.x-xy/(t)*bk.y, -xy/(t)*bk.x+xx/(t)*bk.y);
	float miss=length(li);
	

	write_imagef(dst_g, cds, (float4)((g + li)/1000, miss,1));
}


//iterate current level(not used)
__kernel void solve_k(
		read_only image2d_t src1,
		read_only image2d_t diff,
		read_only image2d_t src_g,
		write_only image2d_t dst_g
		) {
	int2 cds = (int2)(get_global_id(0), get_global_id(1));
	float2 cdsf=convert_float2(cds);
	float2 base =1.0f/(float2)(get_global_size(0)-1,get_global_size(1)-1);

	float xx=0,xy=0,yy=0;
	for(float x=-size;x<=size;x+=1){
		for(float y=-size;y<=size;y+=1){
			float2 pt=cdsf+(float2)(x,y);
			float4 s0=read_imagef(diff, sam_linear, pt);
			xx+=s0.x*s0.x;
			xy+=s0.x*s0.y;
			yy+=s0.y*s0.y;
		}
	}

	float3 sgv=read_imagef(src_g, sam_norm, cdsf * base).xyz;
	float2 g=sgv.xy*1000.0f;
	float miss=sgv.z;

	if(miss>0.005f){
		float2 bk=0;
		for(float x=-size;x<=size;x+=1){
			for(float y=-size;y<=size;y+=1){
				float2 pt=cdsf+(float2)(x,y);
#ifdef GLOBAL_G
				float2 gl=g;
#else
				float2 gl=read_imagef(src_g, sam_linear, pt*base).xy*1000.0f;
#endif

				float ep=read_imagef(diff, sam_linear, pt).z - lumii(read_imagef(src1, sam_linear, pt + gl).xyz);
				bk +=ep * read_imagef(diff, sam_linear, pt).xy;
			}
		}
		float t=xx*yy-xy*xy;
		if(t==0){
			t=1e-10;
		}
		float2 li= (float2)(yy/(t)*bk.x-xy/(t)*bk.y, -xy/(t)*bk.x+xx/(t)*bk.y);
		sgv=(float3)((g + li)/1000, length(li));
	}

	write_imagef(dst_g, cds, (float4)(sgv,1));
}
