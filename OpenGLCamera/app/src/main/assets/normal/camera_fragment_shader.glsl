#extension GL_OES_EGL_image_external : require
precision mediump float;
      	 				
uniform samplerExternalOES s_texture;
varying vec2 v_TextureCoordinates;      	   								
  
void main()                    		
{                              	
    gl_FragColor = texture2D(s_texture, v_TextureCoordinates);
}