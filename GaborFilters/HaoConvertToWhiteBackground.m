function [ output_imseg ] = HaoConvertToWhiteBackground( imseg )

height = size(imseg, 1);
width = size(imseg, 2);
blackPixels = 0;
whitePixels = 0;
output_imseg = imseg;

for i=1:height
    for j = 1:width
        if (imseg(i,j,1) < 125)
            blackPixels = blackPixels + 1;
        else
            whitePixels = whitePixels + 1;
        end
        
    end
end

if (blackPixels > whitePixels)
    for i = 1 : size(imseg, 1)
        for j = 1 : size(imseg, 2)
            for k = 1 : 3
                if (imseg(i,j,k) < 125)
                    output_imseg(i,j,k) = 255;
                else
                    output_imseg(i,j,k) = 0;
                end
            end
        end
    end
end
end

