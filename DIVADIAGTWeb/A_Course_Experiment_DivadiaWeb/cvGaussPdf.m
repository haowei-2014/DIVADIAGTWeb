% cvGaussPdf - Compute the PDF of a multivariate Gaussian
%
% 
%

function Pr = cvGaussPdf(X, Mu, Sigma, normalize, logprob)
if ~exist('normalize', 'var') || isempty(normalize)
    normalize = 'normterm';
end
if ~exist('logprob', 'var') || isempty(logprob)
    logprob = false;
end
[D, N] = size(X);
X = X - repmat(Mu, 1, N);
% Compute N points at burst
Pr = -0.5 * sum((X.' * inv(Sigma)) .* X.', 2) .';
%Pr = -0.5 * diag(X.' * inv(Sigma) * X); % slow
if strcmp(normalize, 'normterm') | normalize == 1
    normterm = log(2*pi)*(D/2) + (abs(det(Sigma))+realmin)*(1/2);
    Pr = Pr - normterm;
elseif strcmp(normalize, 'normsum') | normalize == 2
    Pr = Pr - max(Pr); % pre-normalization to avoid probs being all zero
    % by takin exp because of precision limit (necessary especially in C)
    Pr = Pr - log(sum(exp(Pr)));
end
if ~logprob, Pr = exp(Pr); end;
end