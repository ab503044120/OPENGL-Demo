attribute vec4 a_Position;
attribute vec2 a_TextureCoordinates;
uniform mat4 vMatrix;
uniform mat4 vCoordMatrix;
varying vec2 v_TextureCoordinates;

void main()                    
{                            
    v_TextureCoordinates = vCoordMatrix * a_TextureCoordinates;
    gl_Position = vMatrix * a_Position;
}          