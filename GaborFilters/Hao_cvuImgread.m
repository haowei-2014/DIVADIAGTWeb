function [I] = Hao_cvuImgread(imageArray)

% if the image is color, then convert it to gray.

N = ndims(imageArray);
if (N == 3)
    I = rgb2gray(imageArray);
else
    I = imageArray;
end




