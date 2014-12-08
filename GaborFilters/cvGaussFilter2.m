function [GO, GF] = cvGaussFilter2(I, wsize, Sigma, shape, normalize);
% cvGaussFilter2 - 2D Gaussian filter
%

if ~exist('normalize', 'var') || isempty(normalize)
    normalize = 'normsum';
end
if ~exist('shape', 'var') || isempty(shape)
    shape = 'reflect';
end
if isscalar(Sigma)
    Sigma = [Sigma^2 0; 0 Sigma^2];
end
if isscalar(wsize)
    wysize = floor(wsize/2); wxsize = floor(wsize/2);
else
    wysize = floor(wsize(1)/2); wxsize = floor(wsize(2)/2);
end
if isa(I, 'double') ~= 1
    I = double(I);
end
[nRow, nCol, C] = size(I);

x = -wxsize:wxsize;
y = -wysize:wysize;
[X Y] = meshgrid(x, y);
Z = [reshape(X, [], size(X, 1) * size(X, 2));
    reshape(Y, [], size(Y, 1) * size(Y, 2))];
if strcmp(normalize, 'nonorm') | normalize == 0
    % no normalization
    GF = cvGaussPdf(Z, [0; 0], Sigma, 'nonorm', false);
elseif strcmp(normalize, 'normterm') | normalize == 1
    % gaussian normalization term
    GF = cvGaussPdf(Z, [0; 0], Sigma, 'normterm', false);
elseif strcmp(normalize, 'normsum') | normalize == 2
    % normalize so that sum becomes 1.0
    GF = cvGaussPdf(Z, [0; 0], Sigma, 'normsum', false);
    GF = GF ./ sum(GF);
end
GF = reshape(GF, size(X, 1), size(X, 2));
for c = 1:C
    GO(:,:,c) = cvConv2(I(:,:,c), GF, shape);
end