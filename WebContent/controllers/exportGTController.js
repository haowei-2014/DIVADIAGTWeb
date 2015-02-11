myApp.controller('exportGTController', ['$scope', 'imageName', 'close',
        function ($scope, imageName, close) {
            $scope.imageName = imageName;
            $scope.emailAddress = "";
            // delete .png
 //           var imgNameWithoutSuffix = imageName.substring(0, imageName.indexOf(".")); 
            $scope.xmlName = $scope.imageName + $scope.emailAddress + ".xml";          
            imageName = $scope.imageName;
            emailAddress = $scope.emailAddress;

            $scope.close = function () {
                close({
                    xmlName: $scope.xmlName,
                    emailAddress: emailAddress
                }, 500); // close, but give 500ms for bootstrap to animate
            };
            
            $scope.changeEmailAddress = function (){
                if (!$scope.emailAddress) {
                    emailAddress = "";
                    $scope.xmlName = imageName + ".xml";
                } else{
                    emailAddress = "_" + $scope.emailAddress;
                    $scope.xmlName = imageName + emailAddress + ".xml";
                }
            };
            
            $scope.changeImageName = function (){
                if (!$scope.imageName)
                    imageName = "";
                else
                    imageName = $scope.imageName;
                $scope.xmlName = imageName + emailAddress + ".xml";
            }
    }]);
