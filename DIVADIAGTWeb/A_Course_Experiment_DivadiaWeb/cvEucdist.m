% cvEucdist - Euclidean distance
%

%   
function d = cvEucdist(X, Y)
if ~exist('Y', 'var') || isempty(Y)
    %% Y = zeros(size(X, 1), 1);
    U = ones(size(X, 1), 1);
    d = abs(X'.^2*U).'; return;
end
V = ~isnan(X); X(~V) = 0; % V = ones(D, N); 
U = ~isnan(Y); Y(~U) = 0; % U = ones(D, P); 
d = abs(X'.^2*U - 2*X'*Y + V'*Y.^2);