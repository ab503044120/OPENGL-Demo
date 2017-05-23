attribute vec4 a_Position;
attribute vec4 a_TextureCoordinates;
uniform mat4 vMatrix;
uniform mat4 uTexMtx;
varying vec2 v_TextureCoordinates;

void main()                    
{                            
    v_TextureCoordinates = (uTexMtx * a_TextureCoordinates).xy;
     gl_Position = vMatrix * a_Position;
}