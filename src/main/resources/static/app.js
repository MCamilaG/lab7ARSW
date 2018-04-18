var app = (function () {

    class Point{
        constructor(x,y){
            this.x=x;
            this.y=y;
        }        
    }
    
    var stompClient = null;
    var id = null;

    var addPointToCanvas = function (point) {        
        var canvas = document.getElementById("canvas");
        var ctx = canvas.getContext("2d");
        ctx.beginPath();
        ctx.arc(point.x, point.y, 3, 0, 2 * Math.PI);
        ctx.stroke();
        if (stompClient)
            stompClient.send('/topic/newpoint.' + id,{},JSON.stringify({x:point.x, y:point.y}));
        else
            alert('Debe crear un canal')
    };
    
    
    var getMousePosition = function (evt) {
        canvas = document.getElementById("canvas");
        var rect = canvas.getBoundingClientRect();
        return {
            x: evt.clientX - rect.left,
            y: evt.clientY - rect.top
        };
    };
    
    var actPoint = function (evt) {
        var point = getMousePosition(evt);
        app.publishPoint(point.x, point.y);

    };

    var connectAndSubscribe = () => {
        console.info('Connecting to WS...');
        var socket = new SockJS('/stompendpoint');
        stompClient = Stomp.over(socket);
        
        //subscribe to /topic/TOPICXX when connections succeed
        stompClient.connect({}, frame => {
            console.log(`Connected: ${frame}`);
            stompClient.subscribe(`/topic/newpolygon.${id}`, event => {
                var point = JSON.parse(event.body);
                var canvas = document.getElementById("canvas");

                var ctx = canvas.getContext("2d");
                ctx.beginPath();
                ctx.arc(point.x, point.y, 3, 0, 2 * Math.PI);
                ctx.moveTo(0, 0);
                for (var i = 0; i < point.length - 1; i++) {
                    ctx.moveTo(point[i].x, point[i].y);
                    ctx.lineTo(point[i + 1].x, point[i + 1].y);
                }
                ctx.moveTo(point[point.length - 1].x, point[point.length - 1].y);
                ctx.lineTo(point[0].x, point[0].y);
                ctx.stroke();
            });
            stompClient.subscribe(`/topic/newpoint.${id}`, event => {
                var point = JSON.parse(event.body);
                var canvas = document.getElementById("canvas");
                var ctx = canvas.getContext("2d");
                ctx.beginPath();
                ctx.arc(point.x, point.y, 3, 0, 2 * Math.PI);
                ctx.stroke();   
            });
        });

    };
    
    
    

    return {

        init: function () {
            var can = document.getElementById("canvas");
            if (window.PointEvent) {
                can.addEventListener("pointerdown", actPoint);
            } else {
                can.addEventListener("mousedown", actPoint);
            }
            
            //websocket connection
//            connectAndSubscribe();
        },

        publishPoint: function(px,py){
            var pt=new Point(px,py);
            console.info("publishing point at "+pt);
            addPointToCanvas(pt);

            //publicar el evento
            if(stompClient)
                stompClient.send("/app/newpoint." + id, {}, JSON.stringify(pt));
        },

        disconnect: function () {
            if (stompClient !== null) {
                stompClient.disconnect();
            }
            setConnected(false);
            console.log("Disconnected");
        },
        
        dibuja: function (ide) {
            id = ide;
            connectAndSubscribe();
        }
        
        
    };

})();