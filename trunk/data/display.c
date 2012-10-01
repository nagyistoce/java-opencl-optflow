const sampler_t samin=CLK_NORMALIZED_COORDS_TRUE  | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_LINEAR;


__kernel void filter(read_only image2d_t src, write_only image2d_t dst) {
	int2 cds = (int2)(get_global_id(0), get_global_id(1));
	float2 cdsf = (float2)(get_global_id(0), get_global_id(1))/(float2)(get_global_size(0)-1,get_global_size(1)-1);
	float4 a  = read_imagef(src, samin, cdsf);
	float it=fabs(a.z)*5;
	//a=(float4)(it,it,it,1);
	write_imagef(dst, cds,(float4)(a.xxy,1));
}
