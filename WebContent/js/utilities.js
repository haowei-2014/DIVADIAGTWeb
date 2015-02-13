// distance between 2 points.
function lineDistance(point1, point2) {
    var xs = 0;
    var ys = 0;

    xs = point2.x - point1.x;
    xs = xs * xs;

    ys = point2.y - point1.y;
    ys = ys * ys;

    return Math.sqrt(xs + ys);
}

// check the point p is in between points p1 and p2
function pointInBetween(p, p1, p2) {
    if (p.x >= p1.x && p.x <= p2.x && p.y >= p1.y && p.y <= p2.y) return true;
    if (p.x >= p2.x && p.x <= p1.x && p.y >= p2.y && p.y <= p1.y) return true;
    if (p.x >= p1.x && p.x <= p2.x && p.y <= p1.y && p.y >= p2.y) return true;
    if (p.x >= p2.x && p.x <= p1.x && p.y <= p2.y && p.y >= p1.y) return true;
    return false;
}

// distance between a point and a line
function pointLineDistance(point1, point2, cursor, widthCanvas) {
    var p1x = point1.x;
    var p1y = widthCanvas - point1.y;
    var p2x = point2.x;
    var p2y = widthCanvas - point2.y;
    var cx = cursor.x;
    var cy = widthCanvas - cursor.y;

    // compute the perpendicular distance 
    var a = (p2y - p1y) / (p2x - p1x); // slope
    var c = a * p1x * (-1) + p1y;
    var b = -1;
    var distance = Math.abs(a * cx + b * cy + c) / Math.sqrt(a * a + b * b);

    // computer the perpendicular intersection
    var pa = 1 / a * (-1); // perpendicular slope (a)
    var x = ((-1) * pa * cx - p1y + a * p1x + cy) / (a - pa);
    var y = a * (x - p1x) + p1y;
    y = widthCanvas - y;

    var lower = (p1x >= p2x) ? p2x : p1x;
    var higher = (p1x < p2x) ? p2x : p1x;
    if ((x >= lower) && (x <= higher)) {
        return {
            x: x,
            y: y,
            distance: distance
        };
    } else {
        return null;
    }
}

function getImageName(str) {
    var searchStr = "/";
    var startIndex = 0,
        searchStrLen = searchStr.length;
    var index, indices = [];
    while ((index = str.indexOf(searchStr, startIndex)) > -1) {
        indices.push(index);
        startIndex = index + searchStrLen;
    }
    // if the / is the last character, then pop it
    if (indices[indices.length - 1] == str.length - 1)
        indices.pop();
    // return the string after the last /
    return str.substring(indices[indices.length - 1] + 1);
}

function isNotInautoTextLineRectangles(a, b) {
    for (i = 0; i < b.length; i++) {
        if (a == b[i])
            return false;
    }
    return true;
}