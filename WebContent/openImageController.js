//var myApp = angular.module('myApp', []);

//angular.module('myApp').controller('openImageController', ['$scope', 'imageName', 'close',
myApp.controller('openImageController', ['$scope', 'imageName', 'close',
        function ($scope, imageName, close) {
            $scope.imageName = imageName;
            $scope.emailAddress = "";
            // delete .png
 //           var imgNameWithoutSuffix = imageName.substring(0, imageName.indexOf(".")); 
            $scope.xmlName = $scope.imageName + "_" + $scope.emailAddress + ".xml";
            $scope.close = function () {
                close({
                    xmlName: $scope.xmlName,
                    emailAddress: $scope.emailAddress
                }, 500); // close, but give 500ms for bootstrap to animate
            };
            
            $scope.changeEmailAddress = function (){
 //               imgNameWithoutSuffix = $scope.imageName.substring(0, $scope.imageName.indexOf(".")); 
                $scope.xmlName = $scope.imageName + "_" + $scope.emailAddress + ".xml";
            };
            
            $scope.changeImageName = function (){
//                imgNameWithoutSuffix = $scope.imageName.substring(0, $scope.imageName.indexOf(".")); 
                $scope.xmlName = $scope.imageName + "_" + $scope.emailAddress + ".xml";
            }
    }]);
