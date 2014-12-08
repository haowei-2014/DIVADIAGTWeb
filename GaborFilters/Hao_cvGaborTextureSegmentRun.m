% cvGaborTextureSegmentRun - Run cvGaborTextureSegment
%
% Examples
%  cvGaborTextureSegmentRun('image/data.20.png', 5);
%  cvGaborTextureSegmentRun('image/data.20.png', 5, 'data.20.seg.png');
% This function is used to generate jar file. Input is the image array,
% output is also an image array.
function [imseg] = Hao_cvGaborTextureSegmentRun(imageArray)
%imfile = '/home/hao/workspace/DIVADIAWeb2/DIVADIAGTWeb/WorkData/manualTextBlockInput.png';
%outfile = '/home/hao/workspace/DIVADIAWeb2/DIVADIAGTWeb/WorkData/manualTextBlockOutput.png';

K = 2; 

%I = cvuImgread(imfile);
I = Hao_cvuImgread(imageArray);
[N, M] = size(I);
%% parameter settings
gamma = 1; b = 1; Theta = 0:pi/6:pi-pi/6; phi = 0; shape = 'valid';
% shape = 'same'; % Hao
%% Lambda settings
% (1) Jain's paper %[4 8 16 ...] sqrt(2) 
% Lambda = M./((2.^(2:log2(M/4))).*sqrt(2));  % used by Hao
% (2) J. Zhang's paper
J = (2.^(0:log2(M/8)) - .5) ./ M;
F = [ (.25 - J) (.25 + J) ];
F = sort(F); 
Lambda = 1 ./ F;  %commented by Hao
%% Run
seg = cvGaborTextureSegment(I, K, gamma, Lambda, b, Theta, phi, shape);
%% Display (Upto 5 colors just for now)
%imseg = uint8(seg) * floor(255 / K); % cluster id to gray scale (max 255)
[N, M] = size(seg);
color = [0 0 0; 255 255 255; 255 0 0; 0 255 0; 0 0 255]; % 5 colors reserved
imseg = zeros(N*M, 3);
for i=1:K
    idx = find(seg == i);
    %imseg(idx, :) = repmat(color(i, :), [], length(idx));
    imseg(idx, :) = repmat(color(i, :), length(idx), 1);
end
imseg = reshape(imseg, N, M, 3);
imseg = HaoConvertToWhiteBackground( imseg );

end