const sampler_t sam_norm = CLK_NORMALIZED_COORDS_TRUE  | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_LINEAR;
const sampler_t sam_float = CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_LINEAR;


__kernel void copy(read_only image2d_t src, write_only image2d_t dst) {
	int2 cds = (int2)(get_global_id(0), get_global_id(1));
	float2 cdsf = (float2)(get_global_id(0), get_global_id(1))/(float2)(get_global_size(0)-1,get_global_size(1)-1);
	float4 a  = read_imagef(src, sam_norm, cdsf);
	write_imagef(dst, cds,(float4)(a.xyz,1));
}


__kernel void copy2array(
	read_only image2d_t src,
    __global float *dst)
{
	int2 cds = (int2)(get_global_id(0), get_global_id(1));
	float2 cdsf = (float2)(get_global_id(0), get_global_id(1))/(float2)(get_global_size(0)-1,get_global_size(1)-1);
	float4 a  = read_imagef(src, sam_norm, cdsf);
	int off=cds.y * get_global_size(0)+cds.x;
	dst[off*3]=a.x;
	dst[off*3+1]=a.y;
	dst[off*3+2]=a.x;
}


__kernel void clear(write_only image2d_t dst) {
	int2 coords = (int2)(get_global_id(0), get_global_id(1));
	write_imagef(dst, coords, (float4)(0,0,0,1));
}


__kernel void adjust(
		read_only image2d_t src,
		read_only image2d_t total,
		write_only image2d_t dst) {
	int2 cds = (int2)(get_global_id(0), get_global_id(1));
	float2 cdsf = (float2)(get_global_id(0), get_global_id(1));

	float2 off  = read_imagef(total, sam_float, (float2)(0,0)).xy*2000;

	float4 out= read_imagef(src, sam_float, cdsf+off);

	write_imagef(dst, cds, out);
}
