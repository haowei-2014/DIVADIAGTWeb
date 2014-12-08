Because we will integrate Matlab into our server side, we need to do some configurations.

Install Matlab. The Matlab it should be of linux version; I didn't test windows version. R2013b or newer version is recommended. Then you need to set the information of the directory of matlab. In the class ch.unifr.TextLinesGaborMatlab:

1) on line 14, modify the path of the installation directory of matlab. 
2) on lines 24 and 29, modify the path of the folder "GaborFilters".


