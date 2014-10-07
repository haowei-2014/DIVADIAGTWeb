image = imread('d-273.1.933.186.566.2395.png');
[M N] = size(image)
GaborInput = zeros(512, 512);
[MG NG] = size(GaborInput);

for i=1:MG
    for j = 1:NG
        if j<=N
            if image(i,j) > 125
                GaborInput(i,j)= 255;
            else
                GaborInput(i,j)= 0;
            end
        else
            GaborInput(i,j)= 255;
        end
    end
end

imwrite(GaborInput,'GaborInput.jpg','jpg');



