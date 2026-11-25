shared uint prefixSum[256];

#define STEP(i)  if ((I&(1u<<i))!=0) prefixSum[I] += prefixSum[(I&~((1u<<(i+1))-1))+((1u<<i)-1)];
uint sharedPrefixSumExclusive(uint value) {
    uint I = gl_LocalInvocationID.x;
    prefixSum[I] = value;
    barrier();
    STEP(0)
    barrier();
    STEP(1)
    barrier();
    STEP(2)
    barrier();
    STEP(3)
    barrier();
    STEP(4)
    barrier();
    STEP(5)
    barrier();
    STEP(6)
    barrier();
    STEP(7)
    barrier();
    return prefixSum[I]-value;
}
