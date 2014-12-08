function [I] = cvuImgread(filename)
% cvuImgread - (CV Utility) Read an image as a grayscale image
%
% Synopsis
%   [I] = cvuImgread(filename)
%
% Description
%   cvuImgread reads an image as a grayscale image. 
%   You can ignore the colortype of the image ('indexed' or 'truecolor')
%   using this function. 
%
% Inputs ([]s are optional)
%   (string) filename The image filename
%
% Outputs ([]s are optional)
%   (matrix) I        The grayscale image
%
% See also
%   imread, ind2gray, rgb2gray (Image Processing Toolbox)


info = imfinfo(filename);
[image,map] = imread(filename);
if(strcmp(info.ColorType,'indexed') == 1)
    I = ind2gray(image,map);
elseif(strcmp(info.ColorType,'truecolor') == 1)
    I = rgb2gray(image);
else
    I = image;
end
