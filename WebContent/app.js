// script.js

    // create the module and name it scotchApp
        // also include ngRoute for all our routing needs
    var myApp = angular.module('myApp', ['ngRoute']);

    // configure our routes
    myApp.config(function($routeProvider) {
        $routeProvider

            // route for the home page
            .when('/', {
                templateUrl : 'pages/home.html',
 //               controller  : 'gtingController'
            })

            // route for the about page
            .when('/gting', {
                templateUrl : 'pages/gting.html',
                controller  : 'gtingController'
            })

            // route for the contact page
            .when('/userguide', {
                templateUrl : 'pages/userguide.html',
                controller  : 'userGuideController'
            });
    });

