myApp.controller('showSettingController', ['$scope', 'widthLine', 'close',
        function ($scope, widthLine, close) {

            $scope.widthLine = widthLine;

            $scope.close = function () {
                close({
                    widthLine: $scope.widthLine
                }, 500); // close, but give 500ms for bootstrap to animate
            };
    }]);
