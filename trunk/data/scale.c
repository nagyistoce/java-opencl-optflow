const sampler_t sam_norm=CLK_NORMALIZED_COORDS_TRUE  | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_LINEAR;
const sampler_t sam_int=  CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;

__constant const int r=2;
__constant const int size=r*2+1;
__constant const float k[]={
		0.0066460357f,
		0.19422555f,
		0.59825677f,
		0.19422555f,
		0.0066460357f
};
/*
__constant const float k[]={
		0.0f,
		0.05f,
		0.9f,
		0.05f,
		0.0f
};*/

float lumi(float3 v){
	float3 k=(float3)(0.3f,0.59f,0.11f);
	return dot(v,k);
}
__kernel void filter_h(read_only image2d_t src, write_only image2d_t dst) {
	int2 cds = (int2)(get_global_id(0), get_global_id(1));

	float l=0;
	for(int i=0;i<size;++i){
		float4 a=read_imagef(src, sam_int, (int2)((cds.x*2)+i-r,cds.y));
		l+=lumi(a.xyz)*(k[i]);
	}

	float4 out=(float4)(l,l,l,1);
	write_imagef(dst, cds, out);
}

__kernel void filter_v(read_only image2d_t src, write_only image2d_t dst) {
	int2 cds = (int2)(get_global_id(0), get_global_id(1));

	float l=0;
	for(int i=0;i<size;++i){
		float4 a=read_imagef(src, sam_int, (int2)( cds.x , cds.y*2 + i-r ));
		l+=lumi(a.xyz)*(k[i]);
	}

	float4 out=(float4)(l,l,l,1);
	write_imagef(dst, cds, out);
}




__kernel void filter_old(read_only image2d_t src, write_only image2d_t dst) {
	int2 cds = (int2)(get_global_id(0), get_global_id(1));

	float2 px1=1.0f/(float2)(get_global_size(0)-1,get_global_size(1)-1);
	float2 coords = (float2)(get_global_id(0), get_global_id(1))*px1;

	float2 diff=(1/8.0f)*px1;

	float4 a  = read_imagef(src, sam_norm, coords+(float2)(-diff.x,-diff.y))
			+read_imagef(src, sam_norm, coords+(float2)(-diff.x,+diff.y))
			+read_imagef(src, sam_norm, coords+(float2)(diff.x,diff.y))
			+read_imagef(src, sam_norm, coords+(float2)(+diff.x,-diff.y));

	float l=length(a.xyz/4)/3;
	float4 out=(float4)(l,l,l,1);
	write_imagef(dst, cds, out);
}
