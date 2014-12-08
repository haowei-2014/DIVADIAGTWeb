myApp.controller('openAboutController', ['$scope', 'close',
        function ($scope, close) {
            $scope.close = function () {
                $('.modal-backdrop').remove();
                close({
                    about: "This is about.html",
                }, 500); 
            };
    }]);
