
const sampler_t sam_int=CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;
const sampler_t sam_norm=CLK_NORMALIZED_COORDS_TRUE  | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_LINEAR;
const sampler_t sam_linear= CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_LINEAR;


__constant int size=5;

__kernel void harris(
		read_only image2d_t diff,
		__global float *iout
		) {
	int2 cds = (int2)(get_global_id(0), get_global_id(1));
	float2 cdsf=convert_float2(cds);

	float xx=0,yy=0;


	for(float x=-size;x<=size;x+=1){
		for(float y=-size;y<=size;y+=1){
			float2 pt=cdsf+(float2)(x,y);
			float4 s0=read_imagef(diff, sam_int, pt);
			xx+=s0.x*s0.x;
			yy+=s0.y*s0.y;
		}
	}
	float r=xx*yy-(xx+yy)*(xx+yy)*0.05;
	if(r>2){
		iout[cds.y*get_global_size(0)+cds.x]=r;
	}else{
		iout[cds.y*get_global_size(0)+cds.x]=0;
	}
}


