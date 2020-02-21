document.addEventListener('DOMContentLoaded', init);

function init() {
 

    let burger = document.getElementById("burger");
    let croix = document.getElementById("croix");
    let menu = document.querySelector("header nav");
    burger.addEventListener('click', menuBurger);
    croix.addEventListener('click', menuBurger);
    function menuBurger() {
            if (menu.style.top == "-100vh") {
                croix.style.display = "block";
                burger.style.display = "none";
                menu.style.top = "0vh";

            } else {
                menu.style.top = "-100vh";
                croix.style.display = "none";
                burger.style.display = "block";
            }
    }
    let videos = document.querySelectorAll("video");
    for(let i=0; i <videos.length; i++){
        videos.item(i).addEventListener("click", function(){
            if(videos.item(i).paused){
            videos.item(i).play();
            }else{
                videos.item(i).pause();
            }
        })
 }

initialize();
    
 
function initialize() {
        directionsDisplay = new google.maps.DirectionsRenderer();
        map = new google.maps.Map(document.getElementById("map_canvas"), {
        zoom: 19,
        center: new google.maps.LatLng(46.146842,-1.156965),
        mapTypeId: google.maps.MapTypeId.ROADMAP
      
      });   
    

 
if (navigator.geolocation)
  var watchId = navigator.geolocation.watchPosition(successCallback,null,{enableHighAccuracy:true});
else
  alert("Votre navigateur ne prend pas en compte la géolocalisation HTML5");   

}


function successCallback(position){
  var marker = new google.maps.Marker({
    position: new google.maps.LatLng(position.coords.latitude, position.coords.longitude), 
    map: map
    
  }); 
        var origin = new google.maps.Marker({
    position: new google.maps.LatLng(position.coords.latitude, position.coords.longitude), 
    map: map
  }); 
    
    var destination = new google.maps.Marker({
    position: new google.maps.LatLng(47,-2.25), 
    map: map
    });  
    
    directionsDisplay.setMap(map);
    calcRoute(origin, destination);
    
    
}


function calcRoute(start, end) {

  var request = {
    origin:start.position,
    destination:end.position,
    travelMode: google.maps.TravelMode.DRIVING
  };
    var directionsService = new google.maps.DirectionsService();
  directionsService.route(request, function(result, status) {
    if (status == google.maps.DirectionsStatus.OK) {
      directionsDisplay.setDirections(result);
    }
  });
}

  } 