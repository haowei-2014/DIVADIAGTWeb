image = imread('d-273.1.933.186.566.2395.png');
[M N] = size(image)
width = 1024;
verticalPaddingHeight = 17;

% vertical padding
upperPadding = zeros(verticalPaddingHeight, N);
bottomPadding = zeros(verticalPaddingHeight,N);
for i = 1:verticalPaddingHeight
    for j = 1:N
        upperPadding(i,j) = 255;
        bottomPadding(i,j) = 255;
    end
end
GaborInputTmp = [upperPadding; image; bottomPadding];

% horizontal padding
[MG NG] = size(GaborInputTmp)
leftPaddingWidth = int64((width - N)/2);
rightPaddingWidth = width - N - leftPaddingWidth;

leftPadding = zeros(MG, leftPaddingWidth);
rightPadding = zeros(MG, rightPaddingWidth);

for i = 1: MG
    for j = 1:leftPaddingWidth
        leftPadding(i,j) = 255;
    end
end
for i = 1: MG
    for j = 1:rightPaddingWidth
        rightPadding(i,j) = 255;
    end
end

% for i=1:MG
%     for j = 1:NG
%         if j<=N
%             if image(i,j) > 125
%                 GaborInputTmp(i,j)= 255;
%             else
%                 GaborInputTmp(i,j)= 0;
%             end
%         else
%             GaborInputTmp(i,j)= 255;
%         end
%     end
% end

GaborInput = [leftPadding GaborInputTmp rightPadding];
imwrite(GaborInput,'GaborInput.jpg','jpg');



