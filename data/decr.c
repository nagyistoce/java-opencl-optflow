const sampler_t sam_norm = CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;



__kernel void filter(read_only image2d_t src, write_only image2d_t dst) {
	int2 cds = (int2)(get_global_id(0), get_global_id(1));

	float4 a  = read_imagef(src, sam_norm, cds*2)
			+read_imagef(src, sam_norm, cds*2+(int2)(0,1))
			+read_imagef(src, sam_norm, cds*2+(int2)(1,0))
			+read_imagef(src, sam_norm, cds*2+(int2)(1,1));

	float4 out=(float4)(a.xyz/4,1);
	write_imagef(dst, cds, out);
}
