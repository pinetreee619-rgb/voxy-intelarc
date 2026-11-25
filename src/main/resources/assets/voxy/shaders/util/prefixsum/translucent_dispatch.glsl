#import <voxy:lod/gl46/bindings.glsl>

// Command generation has finished, so the dispatch arguments can be reused for translucents.
void prepareTranslucentDispatch() {
    if (gl_LocalInvocationIndex == 0u) {
        cmdGenDispatchX = max(1u, (translucentDrawCount + TRANSLUCENT_WORK_SIZE - 1u) / TRANSLUCENT_WORK_SIZE);
    }
}
