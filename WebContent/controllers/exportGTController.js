myApp.controller('exportGTController', ['$scope', '$element', 'imageName', 'emailAddress', 'close',
    function($scope, $element, imageName, emailAddress, close) {
        $scope.imageName = imageName;
        $scope.emailAddress = emailAddress;
        // delete .png
        //           var imgNameWithoutSuffix = imageName.substring(0, imageName.indexOf(".")); 
        $scope.xmlName = $scope.imageName + "_" + $scope.emailAddress + ".xml";
        if (emailAddress != "") {
            emailAddress = "_" + emailAddress;
        }

        $scope.close = function() {
            close({
                xmlName: $scope.xmlName,
                emailAddress: emailAddress.substring(1) // remove "_" in the beginning
            }, 500); // close, but give 500ms for bootstrap to animate
        };

        $scope.cancel = function() {
            // Manually hide the modal.
            $element.modal('hide');
            // Now call close, returning control to the caller.
            close({
                xmlName: "",
                emailAddress: ""
            }, 500); // close, but give 500ms for bootstrap to animate
        };

        $scope.changeEmailAddress = function() {
            if (!$scope.emailAddress) {
                emailAddress = "";
                $scope.xmlName = imageName + ".xml";
            } else {
                emailAddress = "_" + $scope.emailAddress;
                $scope.xmlName = imageName + emailAddress + ".xml";
            }
        };

        $scope.changeImageName = function() {
            if (!$scope.imageName)
                imageName = "";
            else
                imageName = $scope.imageName;
            $scope.xmlName = imageName + emailAddress + ".xml";
        }
    }
]);