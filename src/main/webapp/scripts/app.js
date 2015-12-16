var app = angular.module('task', [
    'ngCookies',
    'ngResource',
    'ngSanitize',
    'ngRoute'
]);
 
app.config(function ($routeProvider) {
    $routeProvider.when('/', {
        templateUrl: 'views/list.html',
        controller: 'ListCtrl'
    }).when('/create', {
        templateUrl: 'views/create.html',
        controller: 'CreateCtrl'
    }).otherwise({
        redirectTo: '/'
    });
});
 
app.controller('ListCtrl', function ($scope, $http) {
    $scope.alerts = new Array();
	$http.get(document.location.pathname + 'rest/tasks').success(function (data) {
        $scope.tasks = data;
    }).error(function (data, status) {
    	var alert = new Object();
    	alert.message = "Failed to connect to Backend DataStore!";
    	$scope.alerts.push(alert);
    	console.log('Error ' + data);
    });
	
    $scope.taskStatusChanged = function (task) {
        console.log(task);
        if(task.done)
        	task.completedOn = new Date();
        else
        	task.completedOn = null;
        $http.put(document.location.pathname + 'rest/tasks/' + task.id, task).success(function (data) {
            console.log('status changed');
        }).error(function (data, status) {
        	var alert = new Object();
        	alert.message = "Failed to connect to Backend DataStore!";
        	$scope.alerts.push(alert);
        	console.log('Error ' + data);
        });
    };
    
    $scope.deleteTask = function (task) {
        console.log("Delete task with id " + task.id);
        $http.delete(document.location.pathname + 'rest/tasks/' + task.id).success(function (data) {
        	$scope.tasks = data;
        }).error(function (data, status) {
        	var alert = new Object();
        	alert.message = "Failed to connect to Backend DataStore!";
        	$scope.alerts.push(alert);
        	console.log('Error ' + data);
        });
    };
    
    $scope.generateTasks = function () {
    	console.log('Generating tasks');
    	$http.get(document.location.pathname + 'rest/tasks/generate/20').success(function (data) {
            $scope.tasks = data;
        }).error(function (data, status) {
        	var alert = new Object();
        	alert.message = "Failed to connect to Backend DataStore!";
        	$scope.alerts.push(alert);
        	console.log('Error ' + data);
        });
    };
    $scope.filter = function() {
    	var value = $scope.filter.value;
    	if(value.length > 0) {
	    	$http.get(document.location.pathname + 'rest/tasks/filter/' + $scope.filter.value).success(function (data) {
	            $scope.tasks = data;
	        }).error(function (data, status) {
	        	var alert = new Object();
	        	alert.message = "Failed to connect to Backend DataStore!";
	        	$scope.alerts.push(alert);
	        	console.log('Error ' + data);
	        });
    	} else {
    		$http.get('/mytodo/rest/tasks').success(function (data) {
    	        $scope.tasks = data;
    	    }).error(function (data, status) {
    	    	var alert = new Object();
    	    	alert.message = "Failed to connect to Backend DataStore!";
    	    	$scope.alerts.push(alert);
    	    	console.log('Error ' + data);
    	    });
    	}
	};
});
 
app.controller('CreateCtrl', function ($scope, $http, $location) {
	$scope.task = {
        done: false
    };
	 
    $scope.createTask = function () {
        console.log($scope.task);
        $http.post(document.location.pathname + 'rest/tasks', $scope.task).success(function (data) {
            $location.path('/');
        }).error(function (data, status) {
        	var alert = new Object();
        	alert.message = "Failed to connect to Backend DataStore!";
        	$scope.alerts.push(alert);
        	console.log('Error ' + data);
        });
    };
      
});
