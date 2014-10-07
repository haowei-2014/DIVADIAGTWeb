% cvGaborTextureSegment - Texture Segmentation using Gabor Filters
%
% 
%  
function Seg = cvGaborTextureSegment(I, K, gamma, Lambda, b, Theta, phi, shape)
[nRow, nCol, C] = size(I);

% Step 1. Gabor Filter bank
i = 0;
for lambda = Lambda
    for theta = Theta
        i = i + 1;
        D = cvGaborFilter2(I, gamma, lambda, b, theta, phi, shape, 0);
        % Normalize into [0, 1]
        D = D - min(reshape(D, [], 1)); D = D / max(reshape(D, [], 1));
        %disp('The size of D is:')  % added by Hao
        %size(D)   % added by Hao
        %figure; imshow(uint8(D * 255));
        % Adjust image size to the smallest size if 'valid' (Cut off)
        if (isequal(shape, 'valid') && i >= 2)
            [nRow, nCol, C] = size(O(:, :, i-1));
            [Nr, Nc, C] = size(D);
            DNr = (Nr - nRow)/2;
            DNc = (Nc - nCol)/2;
            D = D(1+floor(DNr):Nr-ceil(DNr), 1+floor(DNc):Nc-ceil(DNc));
        end
%          disp('The size of D is:')  % added by Hao
%          size(D)   % added by Hao
        O(:, :, i) = D;
    end
end
[nRow, nCol, N] = size(O);
size(O) % Hao
disp('The part of gabor filters is finished.')  % added by Hao

% Step 2. Energy (Feature Extraction)
% Step 2-1. Nonlinearity
for i=1:N
    D = O(:, :, i);
    alpha = 1;
    D = tanh(double(O(:, :, i)) .* alpha); % Eq. (3). Input is [0, 1]
    % Normalize into [0, 1] although output of tanh is originally [0, 1]
    D = D - min(reshape(D, [], 1)); D = D / max(reshape(D, [], 1));
    %figure; imshow(uint8(D * 255));
    O(:, :, i) = D;
end
disp('The part of nonlinearity is finished.')  % added by Hao

% Step 2-2. Smoothing
for i=1:N
    D = O(:, :, i);
    lambda = Lambda(floor((i-1)/length(Theta))+1);
    % (1) constant
    % sigma = 5;
    % (2) Use lambda. 0.5 * lambda should be near equal to gabor's sigma
    % sigma = .5 * lambda;
    % (3). Use gabor's sigma
    sigma = (1 / pi) * sqrt(log(2)/2) * (2^b+1) / (2^b-1) * lambda;
    sigma = 3 * sigma;       
    %sigma = 2 * sigma;  % Hao
    D = cvGaussFilter2(D, 2*fix(sigma)+1, sigma, shape, 0);  % Instead of Eq (4), Use Gaussian Filter
    % Normalize into [0, 1]
    D = D - min(reshape(D, [], 1)); D = D / max(reshape(D, [], 1));
    %figure; imshow(uint8(D * 255));
    % Adjust image size to the smallest size if 'valid' (Cut off)
    if (isequal(shape, 'valid') && i >= 2)
        [nRow, nCol, C] = size(P(:, :, i-1));
        [Nr, Nc, C] = size(D);
        DNr = (Nr - nRow)/2;
        DNc = (Nc - nCol)/2;
        D = D(1+floor(DNr):Nr-ceil(DNr), 1+floor(DNc):Nc-ceil(DNc));
    end
    P(:, :, i) = D;
end
O = P; clear P;
[nRow, nCol, N] = size(O);
size(O) % Hao
disp('The part of smoothing is finished.')  % added by Hao


% Step 3. Clustering
% Step 3-1. Adding coordinates information to involves adjacency
for i=1:nRow
    for j=1:nCol
        O(i, j, N+1) = i / nRow; % [0, 1]
        O(i, j, N+2) = j / nCol;
    end
end

% Step 3-2. Clustering
saveFile = reshape(O, [], size(O, 3));
size(saveFile)
dlmwrite('gabor.txt',saveFile,'newline','pc');
data = saveFile.';
% data = reshape(O, [], size(O, 3)).'; % D x N   %commented by Hao
[cluster, codebook] = cvKmeans(data, K);
Seg = reshape(cluster, nRow, nCol, 1); % 2D
end