var myApp = angular.module('myApp', []);

myApp.controller('simpleController', function ($scope) {
    paper.install(window);
    window.onload = function () {
        imgName = "csg562-022.png";
        myEmail = "hao.wei@unifr.ch";
        init();

        function init() {
            paper.setup('canvas');

            img = document.getElementById("parzival");
            // Watch out for the height of the canvas, look at it when compute the point-line distance. 
            //    varcanvas = document.getElementById("canvas");
            //   varcanvas.width = window.innerWidth * 7.5 /12;
            //   alert("window.innerWidth: " + window.innerWidth);
            //   alert("canvas width: " + varcanvas.width);

            imgWidth = img.naturalWidth;
            imgHeight = img.naturalHeight;

            zoom = 0.3;
            raster = new Raster('parzival');
            raster.position = view.center;

            project.activeLayer.scale(zoom);
            $scope.polygon = [];
            $scope.mode = 'draw';
            $scope.color = 'green';
            $scope.regions = [
                {
                    name: 'text line',
                    color: new Color(0, 0.5019607843137255, 0)
                },
                {
                    name: 'text block',
                    color: new Color(0, 0, 1)
                },
                {
                    name: 'decoration',
                    color: new Color(1, 0, 1)
                },
                {
                    name: 'comment',
                    color: new Color(1, 0.6470588235294118, 0)
                },
                {
                    name: 'page',
                    color: new Color(1, 1, 1)
                }
    ];
            $scope.myRegion = $scope.regions[0];
            $scope.shapes = [
                {
                    name: 'polygon',
                },
                {
                    name: 'rectangle',
                }
    ];
            $scope.myShape = $scope.shapes[0];
            $scope.displayText = true;
            $scope.displayTextLine = true;
            $scope.displayDecoration = true;
            $scope.displayComment = true;
            $scope.displayPage = true;
            $scope.$apply();
            currentDrawPath = new Path();
            currentDrawPath.strokeColor = $scope.color;
            currentDrawPath.strokeWidth = 2;
            currentDrawPathLastPoint = null;
            fromRectangle = null;
            opacityPath = 0.1;
            lastClick = 0;
            pathFinished = true;
            tool = new Tool();
            xmlDoc = null;
            mousePosition = $("#mousePosition");

            currentModify = null;
            currentModifyPts = [];
            currentModifyPt = null;
            currentModifyPtCircle = null;
            currentModifyPtIndex = 0;
            currentModifyInfo = {
                type: "",
                currentModifyPt: null,
                currentModifyPtIndex: 0
            };

            colorText = new Color(0, 0, 1);
            colorTextLine = new Color(0, 0.5019607843137255, 0);
            colorDecoration = new Color(1, 0, 1);
            colorComment = new Color(1, 0.6470588235294118, 0);
            colorPage = new Color(1, 1, 1);

            showInfo = true;
            $(document).ready(function () {
                $("#imgName").html("Name: " + imgName);
                $("#imgWidth").html("Width: " + imgWidth + " pixels");
                $("#imgHeight").html("Height: " + imgHeight + " pixels");
            });

            singleClick = true;
            doubleClick = false;
            drag = false;
            hasLastChange = false;
        }

        //    view.onFrame = function (event) {
        //        if (imgChanged) {
        //            imgChanged = false;
        //            img = document.getElementById("parzival");
        //            imgWidth = img.width;
        //            imgHeight = img.height;
        //            alert(imgWidth + " , " + imgHeight);
        //        }
        //    }

        // get the position of the pixel which is being clicked.
        tool.onMouseUp = function (event) {
            //  alert(event.offsetX + "  " + event.offsetY);
            // test if the mousedown is single click, double click, or drag
            singleClick = true;
            doubleClick = false;
            drag = false;
            var d = new Date();
            var t = d.getTime();
            if (event.delta.length != 0) {
                drag = true;
                singleClick = false;
                doubleClick = false;
                document.getElementById("canvas").style.cursor = "auto";
            } else if (t - lastClick < 200) {
                console.log("double"); // double is a single plus a double, bad implementation!
                doubleClick = true;
                singleClick = false;
                drag = false;
            } else {
                console.log("single");
                singleClick = true;
                doubleClick = false;
                drag = false;
            }
            lastClick = t;
            switch ($scope.mode) {
            case "display":
                modifyOrDisplay(event);
                break;
            case "draw":
                draw(event);
                break;
            case "modify":
                modifyOrDisplay(event);
                break;
            }
        }


        function modifyOrDisplay(event) {
            // select the topmost polygon
            if (singleClick && (($scope.mode == "modify") || ($scope.mode == "display"))) {
                var selectedCandidates = [];
                var layerChildren = project.activeLayer.children;
                for (var i = 0; i < layerChildren.length; i++) {
                    if ((layerChildren[i].className == "Path") && layerChildren[i].contains(event.point)) {
                        selectedCandidates.push(layerChildren[i]);
                    }
                }
                if (selectedCandidates.length == 1) {
                    updateCurrentPolygonInfo(selectedCandidates[0]);
                } else if (selectedCandidates.length == 2) {
                    for (var i = 0; i < selectedCandidates.length; i++) {

                        if (!selectedCandidates[i].strokeColor.equals(colorPage)) {
                            updateCurrentPolygonInfo(selectedCandidates[i]);
                        }
                    }
                } else {
                    for (var i = 0; i < selectedCandidates.length; i++) {
                        if (selectedCandidates[i].strokeColor.equals(colorTextLine)) {
                            updateCurrentPolygonInfo(selectedCandidates[i]);
                        }
                    }
                }
                $scope.comments = currentModify.data.comments;
                $scope.$apply();
                console.log(currentModify.data.shape);
            }
            if (drag && ($scope.mode == "modify" || $scope.mode == "display") && (currentModify != null)) {
                currentModify.fillColor = 'red';
                currentModify.opacity = opacityPath;
            }
        }


        function draw(event) {
            // calculate the postion of the pixel respect to the top-left corner of the image.
            //           console.log(raster.bounds.x);
            //           console.log(event.clientX);
            // ATTENTION: take care of the event. When I use canvas.onmouseup = function (event) {}, 
            // I should write in the following way, which is consistent with the next 
            // canvas.addEventListener("mousewheel", function (e) {}
            //           var xClick = Math.round(event.offsetX - raster.bounds.x) / zoom;
            //           var yClick = Math.round(event.offsetY - raster.bounds.y) / zoom;

            var xClick = Math.round((event.point.x - raster.bounds.x) / zoom);
            var yClick = Math.round((event.point.y - raster.bounds.y) / zoom);
            //var scope = $('#xyClick').scope();

            // update the point information of the polygon
            if (xClick < 0 || xClick >= imgWidth || yClick < 0 || yClick >= imgHeight) {
                $(document).ready(function () {
                    $("#xyClick").html("Out of the image!");
                });
            } else if (singleClick) {
                if (pathFinished) {
                    if ($scope.myShape.name == "polygon") {
                        currentDrawPath = new Path();
                        currentDrawPath.add(event.point);
                        currentDrawPathLastPoint = new Point(xClick, yClick);
                        currentDrawPath.data.shape = "polygon";
                        $scope.polygon = [];
                        $scope.polygon.push({
                            x: xClick,
                            y: yClick
                        });
                    } else {
                        currentDrawPath = new Path.Rectangle(event.point, event.point);
                        fromRectangle = new Point(xClick, yClick);
                        currentDrawPath.data.shape = "rectangle";
                        $scope.polygon = [];
                    }
                    currentDrawPath.strokeColor = $scope.color;
                    currentDrawPath.strokeWidth = 2;
                    pathFinished = false;
                } else {
                    if (currentDrawPath.data.shape == "polygon") {
                        currentDrawPath.add(event.point);
                        currentDrawPathLastPoint = new Point(xClick, yClick);
                        $(document).ready(function () {
                            $("#xyClick").html("x: " + xClick + ", y: " + yClick);
                        });
                        $scope.polygon.push({
                            x: xClick,
                            y: yClick
                        });
                    } else {
                        currentDrawPath.remove;
                        var xShow = Math.round(fromRectangle.x * zoom + raster.bounds.x);
                        var yShow = Math.round(fromRectangle.y * zoom + raster.bounds.y);
                        //         currentDrawPath = new Path.Rectangle(new Point(xShow, yShow), event.point);
                        currentDrawPath = new Path();


                        point1Rectangle = new Point(xClick, fromRectangle.y);
                        point2Rectangle = new Point(xClick, yClick);
                        point3Rectangle = new Point(fromRectangle.x, yClick);

                        currentDrawPath.add(new Point(xShow, yShow));
                        currentDrawPath.add(new Point(event.point.x, yShow));
                        currentDrawPath.add(event.point);
                        currentDrawPath.add(new Point(xShow, event.point.y));

                        currentDrawPath.data.shape = "rectangle";
                        currentDrawPath.strokeColor = $scope.color;
                        currentDrawPath.strokeWidth = 2;
                        currentDrawPath.closed = true;
                        pathFinished = true;
                        $(document).ready(function () {
                            $("#xyClick").html("x: " + xClick + ", y: " + yClick);
                        });
                        $scope.polygon.push({
                            x: fromRectangle.x,
                            y: fromRectangle.y
                        }, {
                            x: xClick,
                            y: fromRectangle.y
                        }, {
                            x: xClick,
                            y: yClick
                        }, {
                            x: fromRectangle.x,
                            y: yClick
                        });
                        $scope.$apply();
                        if (xmlDoc == null)
                            initDom();
                        updateDOMDraw();
                        fromRectangle = null;
                    }

                }
            }

            // if double click, then the path is finished.
            if (doubleClick && currentDrawPath.data.shape == "polygon") {
                currentDrawPath.closed = true;
                pathFinished = true;
                currentDrawPathLastPoint = null;
                if (xmlDoc == null)
                    initDom();
                updateDOMDraw();
                //                currentDrawPath.onClick = function (event) {
                //                    updateCurrentPolygonInfo(this);
                //                }
                //currentDrawPath.onMouseLeave = function (event) {}              
            }
            $scope.$apply();
        }

        function updateCurrentPolygonInfo(pathSelected) {
            if (currentModify != null) {
                currentModify.fullySelected = false;
                currentModify.fillColor = null;
                currentModify.opacity = 1;
            }
            pathSelected.fullySelected = true;
            pathSelected.fillColor = 'red';
            pathSelected.opacity = opacityPath;

            if ((currentModify != null) && (currentModify.id != pathSelected.id) && ($scope.mode == "modify")) {
                updateDOMModify();
            }

            currentModify = pathSelected;
            if ($scope.mode == 'modify') {
                var currentModifyPtsLength = currentModify.segments.length;
                if (currentModifyPts.length != 0)
                    currentModifyPts = [];
                for (var i = 0; i < currentModifyPtsLength; i++) {
                    currentModifyPts.push(currentModify.segments[i].point);
                }
            }
            //         $("#id").html(currentModify.id);
        }


        // if you use the modify mode and insert a point, do it and update the current polygon information 
        tool.onMouseDown = function (event) {
            if (($scope.mode == 'modify') && (currentModifyInfo.type == "insert") && (currentModifyPt != null)) {
                currentModify.insert(currentModifyInfo.currentModifyPtIndex + 1, event.point);
                updateCurrentPolygonInfo(currentModify);
            }
        }

        // modify the polygon or pan the image 
        tool.onMouseDrag = function (event) {
            if (currentModifyPtCircle != null)
                currentModifyPtCircle.remove();
            // if modify point exists, check its type and the modify it.
            if (($scope.mode == 'modify') && (currentModifyPt != null)) {
                if (currentModifyInfo.type == "modify") {
                    if (currentModify.data.shape == "polygon")
                        currentModify.segments[currentModifyInfo.currentModifyPtIndex].point = event.point;
                    else {
                        if (currentModifyInfo.currentModifyPtIndex == 0) {
                            currentModify.segments[3].point.x = event.point.x;
                            currentModify.segments[1].point.y = event.point.y;
                        } else if (currentModifyInfo.currentModifyPtIndex == 1) {
                            currentModify.segments[0].point.y = event.point.y;
                            currentModify.segments[2].point.x = event.point.x;
                        } else if (currentModifyInfo.currentModifyPtIndex == 2) {
                            currentModify.segments[1].point.x = event.point.x;
                            currentModify.segments[3].point.y = event.point.y;
                        } else if (currentModifyInfo.currentModifyPtIndex == 3) {
                            currentModify.segments[2].point.y = event.point.y;
                            currentModify.segments[0].point.x = event.point.x;
                        }
                        currentModify.segments[currentModifyInfo.currentModifyPtIndex].point = event.point;
                    }
                } else {
                    currentModify.segments[currentModifyInfo.currentModifyPtIndex + 1].point = event.point;
                }
                updateCurrentPolygonInfo(currentModify);
            } else { // pan the image
                if (($scope.mode == "modify" || $scope.mode == "display") && (currentModify != null)) {
                    currentModify.fillColor = null;
                    currentModify.opacity = 1;
                }
                document.getElementById("canvas").style.cursor = "all-scroll";
                var vector = event.delta;
                project.activeLayer.position = new Point(project.activeLayer.position.x + vector.x,
                    project.activeLayer.position.y + vector.y);
            }
        }


        // update the DOM after modifying the polygon
        function updateDOMModify() {
            $scope.polygon = [];
            for (var i = 0; i < currentModify.segments.length; i++) {
                var xcoordinateImage = Math.round((currentModify.segments[i].point.x - raster.bounds.x) / zoom);
                var ycoordinateImage = Math.round((currentModify.segments[i].point.y - raster.bounds.y) / zoom);
                //      console.log(xcoordinateImage);
                //      console.log(ycoordinateImage);
                if (xcoordinateImage < 0 || xcoordinateImage >= imgWidth || ycoordinateImage < 0 || ycoordinateImage >= imgHeight) {
                    $("#xyClick").html("Out of the image!");
                } else {
                    $("#xyClick").html("");
                    $scope.polygon.push({
                        x: xcoordinateImage,
                        y: ycoordinateImage,
                    });
                }
            }
            //          currentDrawPath = currentModify;
            //           console.log($scope.polygon);

            var page = xmlDoc.getElementsByTagName("Page")[0];
            var textRegions = page.childNodes;
            var currentTextRegion = null;

            if (currentModify.data.idXML) {
                for (var i = 0; i < textRegions.length; i++) {
                    var idXML = textRegions[i].getAttribute("id");
                    var idPathData = currentModify.data.idXML;
                    if (idXML == idPathData) {
                        currentTextRegion = textRegions[i];
                        // this part could be improved. Inserting a point may save time.
                        currentTextRegion.removeChild(currentTextRegion.childNodes[0]);
                    }
                }
            } else {
                for (var i = 0; i < textRegions.length; i++) {
                    var idXML = textRegions[i].getAttribute("id");
                    var idPath = currentModify.id;
                    if (idXML == idPath) {
                        currentTextRegion = textRegions[i];
                        // this part could be improved. Inserting a point may save time.
                        currentTextRegion.removeChild(currentTextRegion.childNodes[0]);
                    }
                }
            }
            newCoords = xmlDoc.createElement("Coords");
            for (var i = 0; i < $scope.polygon.length; i++) {
                newPt = xmlDoc.createElement("Point");
                newPt.setAttribute("y", $scope.polygon[i].y);
                newPt.setAttribute("x", $scope.polygon[i].x);
                newCoords.appendChild(newPt);
            }
            if (currentTextRegion == null) {
                alert("currentTextRegion is null!");
            } else
                currentTextRegion.appendChild(newCoords);
            hasLastChange = true;
        }




        tool.onMouseMove = function (event) {
            mousePosition.html("x: " + Math.round(event.point.x) + ", y: " + Math.round(event.point.y));

            searchPath(event);

            // there are two types of modification: modify the existing corners of the polygon,
            // or insert a point within the existing boundary. Both are to be done with drag.
            if (($scope.mode == 'modify') && (currentModify != null)) {
                //      if (currentModify != null) {
                currentModifyPt = null;
                var distanceCursorToCorner = 100000;
                var cornerFound = false;
                var indexClosest, indexSegment;
                var perpendicularInfo = null;
                var distanceCursorToBacksegment, distanceCursorToForesegment;

                // check the corners of the polygon, to see if any one is close enough to the cursor
                for (var i = 0; i < currentModifyPts.length; i++) {
                    var distanceCursorToCornerI = lineDistance(event.point, currentModifyPts[i]);
                    //   console.log("enough");
                    if (distanceCursorToCornerI < distanceCursorToCorner) {
                        distanceCursorToCorner = distanceCursorToCornerI;
                        indexClosest = i
                    }
                }
                if (distanceCursorToCorner < 20) {
                    currentModifyPtCircle = new Path.Circle({
                        center: [currentModifyPts[indexClosest].x, currentModifyPts[indexClosest].y],
                        radius: 3
                    });
                    currentModifyPtCircle.strokeColor = 'yellow';
                    currentModifyPtCircle.fillColor = 'yellow';
                    currentModifyPtCircle.removeOnMove();

                    currentModifyPt = new Point(currentModifyPts[indexClosest].x, currentModifyPts[indexClosest].y);
                    currentModifyInfo.currentModifyPt = currentModifyPt;
                    currentModifyInfo.type = "modify";
                    currentModifyInfo.currentModifyPtIndex = indexClosest;
                    cornerFound = true;
                }



                // if no corner of the polygon is selected, check if the cursor is close enough to any boundary
                if (!cornerFound) {
                    if (indexClosest != 0)
                        distanceCursorToBacksegment = pointLineDistance(currentModifyPts[indexClosest], currentModifyPts[indexClosest - 1], event.point);
                    else
                        distanceCursorToBacksegment = pointLineDistance(currentModifyPts[0], currentModifyPts[currentModifyPts.length - 1], event.point);

                    if (indexClosest != (currentModifyPts.length - 1))
                        distanceCursorToForesegment = pointLineDistance(currentModifyPts[indexClosest], currentModifyPts[indexClosest + 1], event.point);
                    else
                        distanceCursorToForesegment = pointLineDistance(currentModifyPts[indexClosest], currentModifyPts[0], event.point);

                    if (distanceCursorToBacksegment != null && distanceCursorToForesegment != null) {
                        if (distanceCursorToBacksegment.distance < distanceCursorToForesegment.distance) {
                            if (distanceCursorToBacksegment.distance < 10) {
                                currentModifyPt = new Point(distanceCursorToBacksegment.x, distanceCursorToBacksegment.y);
                                if (indexClosest == 0)
                                    currentModifyInfo.currentModifyPtIndex = currentModifyPts.length - 1;
                                else
                                    currentModifyInfo.currentModifyPtIndex = indexClosest - 1;
                            }
                        } else {
                            if (distanceCursorToForesegment.distance < 20) {
                                currentModifyPt = new Point(distanceCursorToForesegment.x, distanceCursorToForesegment.y);
                                currentModifyInfo.currentModifyPtIndex = indexClosest;
                            }
                        }
                    } else if (distanceCursorToBacksegment == null && distanceCursorToForesegment != null) {
                        if (distanceCursorToForesegment.distance < 20) {
                            currentModifyPt = new Point(distanceCursorToForesegment.x, distanceCursorToForesegment.y);
                            currentModifyInfo.currentModifyPtIndex = indexClosest;
                        }
                    } else if (distanceCursorToBacksegment != null && distanceCursorToForesegment == null) {
                        if (distanceCursorToBacksegment.distance < 20) {
                            currentModifyPt = new Point(distanceCursorToBacksegment.x, distanceCursorToBacksegment.y);
                            if (indexClosest == 0)
                                currentModifyInfo.currentModifyPtIndex = currentModifyPts.length - 1;
                            else
                                currentModifyInfo.currentModifyPtIndex = indexClosest - 1;
                        }
                    }
                    if (currentModifyPt != null) {
                        currentModifyPtCircle = new Path.Circle({
                            center: [currentModifyPt.x, currentModifyPt.y],
                            radius: 3
                        });
                        currentModifyPtCircle.strokeColor = 'yellow';
                        currentModifyPtCircle.fillColor = 'yellow';
                        currentModifyPtCircle.removeOnMove();
                        currentModifyInfo.currentModifyPt = currentModifyPt;
                        currentModifyInfo.type = "insert";
                    }
                }
                //       }
            }
            //        console.log(currentModifyPt);
        }


        // This method is to zoom in/out. After zooming, the pixel under the cursor will move away, so we 
        // have to move it back to the cursor. This is transformed by a little complicated coordinate 
        // transformation. See "Coordinate_transformation.pdf".
        /*canvas.addEventListener("mousewheel", function (e) {

            //        if (currentModifyPt != null)
            //            currentModifyPt.remove();

            //   alert("mousewheel");
            e.preventDefault();
            var direction = e.deltaY;
            var scaleFactor = 1.5;
            var xPToImageLast = Math.round(e.offsetX - raster.bounds.x);
            var yPToImageLast = Math.round(e.offsetY - raster.bounds.y);
            var xPToImageNew;
            var yPToImageNew;

            if (direction < 0) {
                zoom = zoom * scaleFactor;
                project.activeLayer.scale(scaleFactor);
                xPToImageNew = xPToImageLast * scaleFactor;
                yPToImageNew = yPToImageLast * scaleFactor;
            } else {
                zoom = zoom / scaleFactor;
                project.activeLayer.scale(1 / scaleFactor);
                xPToImageNew = xPToImageLast / scaleFactor;
                yPToImageNew = yPToImageLast / scaleFactor;
            }

            var xPToCanvasNew = xPToImageNew + Math.round(raster.bounds.x);
            var yPToCanvasNew = yPToImageNew + Math.round(raster.bounds.y);
            var offsetXFromPToCursor = Math.round(e.offsetX - xPToCanvasNew);
            var offsetYFromPToCursor = Math.round(e.offsetY - yPToCanvasNew);
            //     raster.position += new Point(offsetXFromPToCursor, offsetYFromPToCursor);
            project.activeLayer.position = new Point(raster.position.x + offsetXFromPToCursor,
                raster.position.y + offsetYFromPToCursor);
            view.draw();
        });*/

        /*canvas.addEventListener("mousewheel", function (e) {

            //        if (currentModifyPt != null)
            //            currentModifyPt.remove();

            //   alert("mousewheel");
            e.preventDefault();
            var direction = e.deltaY;
            var scaleFactor = 1.5;


            if (direction < 0) {
                zoom = zoom * scaleFactor;
                project.activeLayer.scale(scaleFactor, new Point(e.offsetX, e.offsetY));

            } else {
                zoom = zoom / scaleFactor;
                project.activeLayer.scale(1 / scaleFactor, new Point(e.offsetX, e.offsetY));

            }


            view.draw();
        });*/

        $('#canvas').bind('mousewheel DOMMouseScroll MozMousePixelScroll',
            function (e) {
                var delta = 0;
                var zoomTrail = 0;
                e.preventDefault();
                var scaleFactor = 1.5;

                if (e.offsetX == undefined) // this works for Firefox
                {
                    xpos = e.originalEvent.layerX;
                    ypos = e.originalEvent.layerY;
                    console.log(xpos);
                    console.log(ypos);
                } else // works in Google Chrome
                {
                    xpos = e.offsetX;
                    ypos = e.offsetY;
                }

                if (e.type == 'mousewheel') { //this is for chrome/IE
                    delta = e.originalEvent.wheelDelta;
                } else if (e.type == 'DOMMouseScroll') { //this is for FireFox
                    delta = e.originalEvent.detail * -1; //FireFox reverses the scroll so we force to reverse.
                }
                if (delta > 0) { //scroll up
                    zoomTrail = zoom * scaleFactor;
                    if (zoomTrail < 5) {
                        zoom = zoom * scaleFactor;
                        project.activeLayer.scale(scaleFactor, new Point(xpos, ypos));
                    }
                } else if (delta < 0) { //scroll down 
                    zoomTrail = zoom / scaleFactor;
                    if (zoomTrail > 0.1) {
                        zoom = zoom / scaleFactor;
                        project.activeLayer.scale(1 / scaleFactor, new Point(xpos, ypos));
                    }
                }
                view.update();
            });


        $scope.testZoomin = function () {

            //     alert("click test");

            var scaleFactor = 1.5;
            zoom = zoom * scaleFactor;
            project.activeLayer.scale(scaleFactor);
            myCanvas.width *= scaleFactor;
            myCanvas.height *= scaleFactor;
            raster.position = new Point(myCanvas.offsetLeft + myCanvas.offsetWidth / 2, myCanvas.offsetTop + myCanvas.offsetHeight / 2);
            view.draw();
        }

        /*$scope.test = function () {

  
        }*/


        $scope.removePolygon = function () {
            var page = xmlDoc.getElementsByTagName("Page")[0];
            var textRegions = page.childNodes;
            for (var i = 0; i < textRegions.length; i++) {
                var idXML = textRegions[i].getAttribute("id");
                var idXMLPath = currentModify.data.idXML;
                if ((idXML == idXMLPath) || (idXML == currentDrawPath.id)) {
                    page.removeChild(textRegions[i]);
                }
            }
            currentModify.remove();
        }


        $scope.modeChange = function () {
            if ($scope.mode == 'draw') {
                document.getElementById("drawForm").style.display = "";
                document.getElementById("modifyForm").style.display = "none";
                document.getElementById("displayForm").style.display = "none";
            } else if ($scope.mode == 'modify') {
                document.getElementById("drawForm").style.display = "none";
                document.getElementById("modifyForm").style.display = "";
                document.getElementById("displayForm").style.display = "none";
            } else {
                document.getElementById("drawForm").style.display = "none";
                document.getElementById("modifyForm").style.display = "none";
                document.getElementById("displayForm").style.display = "";
            }
        }


        $scope.displayChange = function () {
            //       console.log(project.activeLayer.children);
            var layerChildren = project.activeLayer.children;
            for (var i = 0; i < layerChildren.length; i++) {
                if (layerChildren[i].className == "Path") {
                    if (layerChildren[i].strokeColor.equals(colorTextLine)) {
                        if ($scope.displayTextLine)
                            layerChildren[i].visible = true;
                        else
                            layerChildren[i].visible = false;
                    }
                    if (layerChildren[i].strokeColor.equals(colorText)) {
                        if ($scope.displayText)
                            layerChildren[i].visible = true;
                        else
                            layerChildren[i].visible = false;
                    }
                    if (layerChildren[i].strokeColor.equals(colorDecoration)) {
                        if ($scope.displayDecoration)
                            layerChildren[i].visible = true;
                        else
                            layerChildren[i].visible = false;
                    }
                    if (layerChildren[i].strokeColor.equals(colorComment)) {
                        if ($scope.displayComment)
                            layerChildren[i].visible = true;
                        else
                            layerChildren[i].visible = false;
                    }
                    if (layerChildren[i].strokeColor.equals(colorPage)) {
                        if ($scope.displayPage)
                            layerChildren[i].visible = true;
                        else
                            layerChildren[i].visible = false;
                    }
                }
            }
            view.draw();
        }


        $scope.editComments = function () {
            console.log($scope.comments);

            var page = xmlDoc.getElementsByTagName("Page")[0];
            var textRegions = page.childNodes;
            var idXMLPath;
            for (var i = 0; i < textRegions.length; i++) {
                var idXML = textRegions[i].getAttribute("id");
                if ($scope.mode == "draw")
                    idXMLPath = currentDrawPath.data.idXML;
                else
                    idXMLPath = currentModify.data.idXML;
                if ((idXML == idXMLPath) || (idXML == currentDrawPath.id)) {
                    console.log("Found it!");
                    textRegions[i].setAttribute("comments", $scope.comments);
                }
            }
        }


        // import the ground truth
        $scope.importGT = function () {
            // click the <input type = 'file'> by program

            $(document).ready(function () {
                $('#myInput').click();
            });
        }

        $(document).ready(function () {
            // do import event whenever #myInput is closed.
            $("#myInput").change(function () {
                var fileToLoad = document.getElementById("myInput").files[0];
                //   var fileToLoad = document.getElementById("fileToLoad").files[0];
                var fileReader = new FileReader();
                fileReader.onload = function (fileLoadedEvent) {
                    var textFromFileLoaded = fileLoadedEvent.target.result;
                    // document.getElementById("inputTextToSave").value = textFromFileLoaded;
                    drawGT(textFromFileLoaded);
                };
                fileReader.readAsText(fileToLoad, "UTF-8");
                //       var fileText = fileReader.result;
            });
        });



        function updateDOMDraw() {
            var page = xmlDoc.getElementsByTagName("Page")[0];
            newCd = xmlDoc.createElement("Coords");
            for (var i = 0; i < $scope.polygon.length; i++) {
                newPt = xmlDoc.createElement("Point");
                newPt.setAttribute("y", $scope.polygon[i].y);
                newPt.setAttribute("x", $scope.polygon[i].x);
                newCd.appendChild(newPt);
            }
            newTR = xmlDoc.createElement("TextRegion");
            newTR.setAttribute("comments", "");
            newTR.setAttribute("custom", "0");
            newTR.setAttribute("id", currentDrawPath.id);
            if (currentDrawPath.strokeColor.equals(colorTextLine))
                newTR.setAttribute("type", "textline");
            if (currentDrawPath.strokeColor.equals(colorText))
                newTR.setAttribute("type", "text");
            if (currentDrawPath.strokeColor.equals(colorDecoration))
                newTR.setAttribute("type", "decoration");
            if (currentDrawPath.strokeColor.equals(colorComment))
                newTR.setAttribute("type", "comment");
            if (currentDrawPath.strokeColor.equals(colorPage))
                newTR.setAttribute("type", "page");
            newTR.setAttribute("id", currentDrawPath.id);
            newTR.appendChild(newCd);
            page.appendChild(newTR);
            hasLastChange = true;
        }


        $scope.exportGT = function () {
            if (xmlDoc != null) {
                if (hasLastChange)
                    editLastChange();
                if (currentModify != null) {
                    updateDOMModify();
                }
                var textToWrite = (new XMLSerializer()).serializeToString(xmlDoc);
                //     alert(textToWrite);
                var textFileAsBlob = new Blob([textToWrite], {
                    type: 'text/xml'
                });
                //       var fileNameToSaveAs = document.getElementById("inputFileNameToSaveAs").value;
                var fileNameToSaveAs = imgName.substring(0, imgName.indexOf('.')) + "_" + myEmail + ".xml";
                var downloadLink = document.createElement("a");
                downloadLink.download = fileNameToSaveAs;
                downloadLink.innerHTML = "Download File";
                if (window.webkitURL != null) {
                    // Chrome allows the link to be clicked
                    // without actually adding it to the DOM.
                    downloadLink.href = window.webkitURL.createObjectURL(textFileAsBlob);
                } else {
                    // Firefox requires the link to be added to the DOM
                    // before it can be clicked.
                    downloadLink.href = window.URL.createObjectURL(textFileAsBlob);
                    downloadLink.onclick = destroyClickedElement;
                    downloadLink.style.display = "none";
                    document.body.appendChild(downloadLink);
                }
                downloadLink.click();
            }
        }

        function destroyClickedElement(event) {
            document.body.removeChild(event.target);
        }


        $scope.importImg = function () {
            $(document).ready(function () {
                $('#myImg').click();
            });
        }

        // load new image. Reference to test3.html, or check email "useful posts.."
        /*$scope.fileNameChanged = function (event) {
            console.log("select file");
            var selectedFile = event.target.files[0];
            var reader = new FileReader();
            //    var imgtag = document.getElementById("myimage");
            //    imgtag.title = selectedFile.name;
            reader.onload = function (event) {
                //    imgtag.src = event.target.result;
                document.getElementById("parzival").src = event.target.result;
                console.log(event.target);
                console.log(event.target.result);
                imgName = selectedFile.name;
                init();
            };
            reader.readAsDataURL(selectedFile);
        }*/
        
        $(document).ready(function (event) {
            // do import event whenever #myInput is closed.
            $("#myImg").change(function (event) {
                var fileToLoad = event.target.files[0];
                var fileReader = new FileReader();
                fileReader.onload = function (event) {
                	document.getElementById("parzival").src = event.target.result;
                	imgName = fileToLoad.name;
                    init();
                };
                fileReader.readAsDataURL(fileToLoad);
                //       var fileText = fileReader.result;
            });
        });


        $scope.showImgInfo = function () {
            if (showInfo) {
                document.getElementById("imgInfo").style.display = "none";
                document.getElementById("showInfoButton").innerHTML = "Show info";
            } else {
                document.getElementById("imgInfo").style.display = "";
                document.getElementById("showInfoButton").innerHTML = "Hide info";
            }
            showInfo = !showInfo;
        }

        // draw ground truth on the canvas
        function drawGT(x) {
            xmlDoc = loadXMLString(x);

            var currentDrawPath;
            var page = xmlDoc.getElementsByTagName("Page")[0];
            var textRegions = page.childNodes;

            for (i = 0; i < textRegions.length; i++) {
                var points = textRegions[i].childNodes[0].childNodes;
                currentDrawPath = new Path();
                for (var j = 0; j < points.length; j++) {
                    pointPath = points[j];
                    var x = pointPath.getAttribute("x");
                    var y = pointPath.getAttribute("y");
                    // transform the coordinate to display it
                    x = x * zoom + raster.bounds.x;
                    y = y * zoom + raster.bounds.y;
                    currentDrawPath.add(new Point(x, y));
                }
                if (points.length == 4 && points[0].getAttribute("y") == points[1].getAttribute("y") &&
                    points[1].getAttribute("x") == points[2].getAttribute("x") &&
                    points[2].getAttribute("y") == points[3].getAttribute("y"))
                    currentDrawPath.data.shape = "rectangle";
                else
                    currentDrawPath.data.shape = "polygon";

                // assign color to different classes
                switch (textRegions[i].getAttribute("type")) {
                case "textline":
                    currentDrawPath.strokeColor = 'green';
                    break;
                case "decoration":
                    currentDrawPath.strokeColor = 'magenta';
                    break;
                case "comment":
                    currentDrawPath.strokeColor = 'orange';
                    break;
                case "text":
                    currentDrawPath.strokeColor = 'blue';
                    break;
                case "page":
                    currentDrawPath.strokeColor = 'white';
                    break;
                }
                currentDrawPath.strokeWidth = 2;
                currentDrawPath.data.idXML = textRegions[i].getAttribute("id");
                currentDrawPath.data.comments = textRegions[i].getAttribute("comments");
                currentDrawPath.closed = true;

                /*if (points.length > 1) {
                        

                    var samePoint = 1;

                    if (points.length == 2) {
                        console.log("here");
                        var thisID = textRegions[i].getAttribute("id");
                        console.log(thisID);
                    }
                    pointPath = points[0];
                    var x = pointPath.getAttribute("x");
                    var y = pointPath.getAttribute("y");

                    for (var j = 1; j < points.length; j++) {
                        var pointPathNext = points[j];
                        var xNext = pointPathNext.getAttribute("x");
                        var yNext = pointPathNext.getAttribute("y");

                        if (x == xNext && y == yNext) {
                            samePoint += 1;
                        }
                    }

                    if (samePoint != points.length) {
                        for (var j = 0; j < points.length; j++) {
                            pointPath = points[j];
                            var x = pointPath.getAttribute("x");
                            var y = pointPath.getAttribute("y");
                            // transform the coordinate to display it
                            x = x * zoom + raster.bounds.x;
                            y = y * zoom + raster.bounds.y;
                            currentDrawPath.add(new Point(x, y));
                        }
                        currentDrawPath.data.idXML = textRegions[i].getAttribute("id");
                        currentDrawPath.data.comments = textRegions[i].getAttribute("comments");
                        currentDrawPath.data.shape = "polygon";
                        currentDrawPath.closed = true;
                    }
                }*/

            }
        }


        function initDom() {
            text = "<PcGts><Metadata>";
            text = text + "<Creator>hao.wei@unifr.ch</Creator>";
            text = text + "<Created>15.07.2014</Created>";
            text = text + "<LastChange>16.07.2014</LastChange>";
            text = text + "<Comment></Comment>";
            text = text + "</Metadata>";
            text = text + "<Page></Page>";
            text = text + "</PcGts>";
            xmlDoc = loadXMLString(text);

            var pcGts = xmlDoc.getElementsByTagName("PcGts")[0];
            pcGts.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            pcGts.setAttribute("xsi:schemaLocation", "http://schema.primaresearch.org/PAGE/gts/pagecontent/2013-07-15 http://schema.primaresearch.org/PAGE/gts/pagecontent/2013-07-15/pagecontent.xsd");
            pcGts.setAttribute("pcGtsId", "");

            var d = new Date();
            var created = xmlDoc.getElementsByTagName("Created")[0];
            created.childNodes[0].nodeValue = d;
            var creator = xmlDoc.getElementsByTagName("Creator")[0];
            creator.childNodes[0].nodeValue = myEmail;

            var page = xmlDoc.getElementsByTagName("Page")[0];
            page.setAttribute("imageWidth", imgWidth);
            page.setAttribute("imageHeight", imgHeight);
            page.setAttribute("imageFilename", imgName);
        }


        function editLastChange() {
            var d = new Date();
            var lastChange = xmlDoc.getElementsByTagName("LastChange")[0];
            lastChange.childNodes[0].nodeValue = d;
        }

        function loadXMLString(txt) {
            if (window.DOMParser) {
                parser = new DOMParser();
                xmlDoc = parser.parseFromString(txt, "text/xml");
            } else // Internet Explorer
            {
                xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
                xmlDoc.async = false;
                xmlDoc.loadXML(txt);
            }
            return xmlDoc;
        }

        function searchPath(event) {
            if (currentDrawPath.data.shape == "rectangle" && fromRectangle) {
                var xShow = Math.round(fromRectangle.x * zoom + raster.bounds.x);
                var yShow = Math.round(fromRectangle.y * zoom + raster.bounds.y);
                var searchingRectangle = new Path.Rectangle(new Point(xShow, yShow), event.point);
                searchingRectangle.strokeColor = $scope.color;
                searchingRectangle.strokeWidth = 2;
                searchingRectangle.removeOnMove();
            } else if (currentDrawPath.data.shape == "polygon" && currentDrawPathLastPoint) {
                var searchingPath = new Path();
                searchingPath.strokeColor = currentDrawPath.strokeColor;
                searchingPath.strokeWidth = 2;
                var xShow = Math.round(currentDrawPathLastPoint.x * zoom + raster.bounds.x);
                var yShow = Math.round(currentDrawPathLastPoint.y * zoom + raster.bounds.y);
                searchingPath.add(new Point(xShow, yShow));
                searchingPath.add(event.point);
                searchingPath.removeOnMove();
            }
        }


        function loadXMLDoc(filename) {
            if (window.XMLHttpRequest) {
                xhttp = new XMLHttpRequest();
            } else // code for IE5 and IE6
            {
                xhttp = new ActiveXObject("Microsoft.XMLHTTP");
            }
            xhttp.open("GET", filename, false);
            xhttp.send();
            return xhttp.responseXML;
        }
        
        // A crucial function to send the image to the server. 
        // imageUrl is a base64 string representing the image. imageUrl is extremely long, so we have to remove the 
        // limit of the ajax POST size. See:
        // http://www.enterprise-architecture.org/documentation/doc-administration/145-post-size-limit
        // http://stackoverflow.com/questions/12194997/unable-to-change-tomcat-users-xml-and-server-xml-while-tomcat7-runs-within
        $(document).ready(function() {                          
            $('#autoSegment').click(function() {
            	document.getElementById("autoSegmentComment").innerHTML = "Please wait for a few seconds!";
            	var imageUrl = document.getElementById("parzival").src;      
                $.post('AutoSegmentServlet', 
                  {
                	imageName:imgName,
                	imageURL:imageUrl
                  },
                  function(responseJson) {    
                	console.log(responseJson);  
                	processResponseJson(responseJson);
                });  
            });
        });
        
        function processResponseJson(responseJson){
        	if (responseJson.textBlocks){
        		drawAutoResult(responseJson.textBlocks, "textBlocks");
        	}
        	if (responseJson.page){
        		drawAutoResult(responseJson.page, "page");
        	}
        	if (responseJson.textLines){
        		drawAutoResult(responseJson.textLines, "textLines");
        	}
        	view.update();
        }
        
        $(document).ready(function() {                          
            $('#loadDatabase').click(function() {
            	document.getElementById("autoSegmentComment").innerHTML = "test one";    
            	$.post('GetImageServlet', 
                        {
                      	imageName:imgName
                        },
                        function(responseJson) {    
                      	console.log(responseJson);  
                      	document.getElementById("parzival").src = responseJson;
                        init();
                      });  
            });
        });

        function drawAutoResult(textRegions, regionType){
      //  	alert("call drawAutoResult");            
        	for (var i = 0; i < textRegions.length; i++){
        		var points = textRegions[i];
                var currentDrawPath = new Path();
                for (var j = 0; j < points.length; j++) {
                    pointPath = points[j];
                    var x = pointPath[0];
                    var y = pointPath[1];
                    // transform the coordinate to display it
                    x = x * zoom + raster.bounds.x;
                    y = y * zoom + raster.bounds.y;
                    currentDrawPath.add(new Point(x, y));
                }
                if (points.length == 4 && points[0][1] == points[1][1] &&
                    points[1][0] == points[2][0] &&
                    points[2][1] == points[3][1])
                    currentDrawPath.data.shape = "rectangle";
                else
                    currentDrawPath.data.shape = "polygon";

                // assign color to different classes
                switch (regionType) {
                case "textBlocks":
                    currentDrawPath.strokeColor = 'blue';
                    break;
                case "page":
                    currentDrawPath.strokeColor = 'red';
                    break;
                case "textLines":
                    currentDrawPath.strokeColor = 'green';
                    document.getElementById("autoSegmentComment").innerHTML = "";
                    break;
                }
                currentDrawPath.strokeWidth = 2;
                currentDrawPath.closed = true;
            //    alert("done!");
        	}
        }
        
        
      
    };
});

/*myApp.config(function ($compileProvider) {
    $compileProvider.aHrefSanitizationWhitelist(/^\s*(|blob|):/);
});*/