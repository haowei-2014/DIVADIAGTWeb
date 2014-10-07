function [GO, GF] = cvGaborFilter2(I, gamma, lambda, b, theta, phi, shape, normalize);
% cvGaborFilter2 - 2D Gabor filter
%
%
% 
%   
if ~exist('normalize', 'var') || isempty(normalize)
    normalize = 'normsum';
end
if ~exist('shape', 'var') || isempty(shape)
    shape = 'reflect';
end
if isa(I, 'double') ~= 1
    I = double(I);
end

sigma = (1 / pi) * sqrt(log(2)/2) * (2^b+1) / (2^b-1) * lambda;
Sy = sigma * gamma;
for x = -fix(sigma):fix(sigma)
    for y = -fix(Sy):fix(Sy)
        xp = x * cos(theta) + y * sin(theta);
        yp = y * cos(theta) - x * sin(theta);
        yy = fix(Sy)+y+1;
        xx = fix(sigma)+x+1;
        GF(yy,xx) = exp(-.5*(xp^2+gamma^2*yp^2)/sigma^2) * cos(2*pi*xp/lambda+phi);  % real part
%        GF(yy,xx) = exp(-.5*(xp^2+gamma^2*yp^2)/sigma^2) * sin(2*pi*xp/lambda+phi);  % imaginary part
        % magnitude
%         real = exp(-.5*(xp^2+gamma^2*yp^2)/sigma^2) * cos(2*pi*xp/lambda+phi);
%         imaginary = exp(-.5*(xp^2+gamma^2*yp^2)/sigma^2) * sin(2*pi*xp/lambda+phi);
%         GF(yy,xx) = sqrt(real^2 + imaginary^2);
    end
end
if strcmp(normalize, 'normterm') | normalize == 1
    GF = GF ./ (2*pi*(sigma^2/gamma));
elseif strcmp(normalize, 'normsum') | normalize == 2
    GF = GF ./ sum(sum(GF));
end
GO = cvConv2(I, double(GF), shape);