function C = cvConv2(A, B, shape)
% cvConv2 - 2-D convolution

%   
if ~exist('shape', 'var') || isempty(shape)
    shape = 'reflect';
end
if ndims(A) >= 3 || ndims(B) >= 3
    error('The 1st and 2nd inputs must be a two dimensional array.');
end
if strcmp(shape, 'reflect')
    A = cvuReflectBoundary(A, size(B));
    C = conv2(A, B, 'valid');
else
    C = conv2(A, B, shape);
end

% function C = icvConv2(A, B)
% % icvConv2 - Own implementation of 2-D convolution
% % Ignore outer space (eq. zero padding outside boundaries)
% [ma na] = size(A);
% [mb nb] = size(B);
% MaskRow = -floor(mb/2):ceil(mb/2-1);
% MaskCol = -floor(nb/2):ceil(nb/2-1);
% for n = 1:na
%      for m = 1:ma
%          M = m + MaskRow;
%          N = n + MaskCol;
%          mask = B(M >= 1 & M <= ma, N >= 1 & N <= na);
%          source = A(M(M >= 1 & M <= ma), N(N >= 1 & N <= na));
%          C(m, n) = sum(sum(source .* mask));
%      end
% end