const sampler_t samin=CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;

__constant int size=1;
/*__kernel void erosion(read_only image2d_t src, write_only image2d_t dst) {
	int2 coords = (int2)(get_global_id(0), get_global_id(1));
	float3 minv=(float3)(1e6);
	float minl=1e6;
	for(int i=-size;i<=size;++i){
		for(int j=-size;j<=size;++j){
			float3 a= read_imagef(src, samin, coords+(int2)(i,j)).xyz;
			float l=length(a.xy);
			if(minl>l){
				minl=l;
				minv=a;
			}
		}
	}
	write_imagef(dst, coords, (float4)(minv,1));
}
*/

__kernel void erosion(read_only image2d_t src, write_only image2d_t dst) {
	int2 coords = (int2)(get_global_id(0), get_global_id(1));
	for(int i=-size;i<=size;++i){
		for(int j=-size;j<=size;++j){
			float3 a = read_imagef(src, samin, coords+(int2)(i,j)).xyz*1000;
			float l=length(a.xy);
			if(l<1){
				write_imagef(dst, coords, (float4)(0,0,0,1));
				return;
			}
		}
	}
	write_imagef(dst, coords, read_imagef(src, samin, coords));
}
