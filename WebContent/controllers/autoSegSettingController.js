//var myApp = angular.module('myApp', []);

//angular.module('myApp').controller('autoSegSettingController', ['$scope', 'linkingRectWidth', 'linkingRectHeight', 'close',
myApp.controller('autoSegSettingController', ['$scope', 'linkingRectWidth', 'linkingRectHeight', 'close',
        function ($scope, linkingRectWidth, linkingRectHeight, close) {
            $scope.linkingRectWidth = linkingRectWidth;
            $scope.linkingRectHeight = linkingRectHeight;

            $scope.close = function () {
                close({
                    linkingRectWidth: $scope.linkingRectWidth,
                    linkingRectHeight: $scope.linkingRectHeight
                }, 500); // close, but give 500ms for bootstrap to animate
            };
    }]);
