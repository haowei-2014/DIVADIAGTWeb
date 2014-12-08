myApp.controller('testModalController', ['$scope', 'title', 'close',
    function($scope, title, close) {
        $scope.name = "aafdasfsadfasdfasfdasdfafdasfda";
        $scope.age = 18;
        $scope.title = title;
        $scope.close = function() {
            close({
                name: $scope.name,
                age: $scope.age
            }, 500); // close, but give 500ms for bootstrap to animate
        };
    }
]);