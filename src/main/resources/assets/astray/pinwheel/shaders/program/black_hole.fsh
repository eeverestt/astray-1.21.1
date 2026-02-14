#include veil:space_helper
#define M_PI 3.1415926535897932384626433832795

uniform sampler2D DiffuseSampler0;
uniform sampler2D DiffuseDepthSampler;

uniform vec3 pos;
uniform float timer;
uniform float GameTime;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec3 baseColor = texture(DiffuseSampler0, texCoord).rgb;

    float cylinderRadius = sin(min(timer / 20.0, M_PI / 2.0)) * 14.0;
    float cylinderHeight = 30.0;

    float rimWidth = 0.2;
    float darkFactor = 0.2;

    float depthSample = texture(DiffuseDepthSampler, texCoord).r;
    vec3 sceneWorldPos = screenToWorldSpace(texCoord, depthSample).xyz;

    vec3 diff = sceneWorldPos - pos;
    float distToAxis = length(diff.xz);

    float heightMask = step(-cylinderHeight * 0.5, diff.y) * (1.0 - step(cylinderHeight * 0.5, diff.y));

    float insideMask = (1.0 - step(cylinderRadius, distToAxis)) * heightMask;

    float rimInner = cylinderRadius - rimWidth;
    float rimMask = step(rimInner, distToAxis) * (1.0 - step(cylinderRadius, distToAxis)) * heightMask;

    float bottomRimHeight = rimWidth;
    float bottomMask = step(-cylinderHeight * 0.5, diff.y) * (1.0 - step(-cylinderHeight * 0.5 + bottomRimHeight, diff.y)) * step(distToAxis, cylinderRadius);

    float totalRimMask = rimMask + bottomMask;

    float innerOnlyMask = insideMask * (1.0 - totalRimMask);

    vec3 rimColor = vec3(0.7, 0.0, 0.7);
    vec3 darkColor = baseColor * darkFactor;

    vec3 color = baseColor;
    color = mix(color, darkColor, innerOnlyMask);
    color = mix(color, rimColor, totalRimMask);

    fragColor = vec4(color, 1.0);
}

