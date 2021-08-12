<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="/WEB-INF/tlds/xmlmsg.tld" prefix="xmlmsg" %>

<html>
  <head>
    <title>Map</title>
    <script
      src="https://maps.googleapis.com/maps/api/js?key=AIzaSyAx2F7DTpv-xsycdYothvMI80b4PoZvrS8&callback=initMap&libraries=places&v=weekly"
      defer
    ></script>
	<%@ include file="/livesite/DataCapture/Callout/CalloutProxy.jsp" %>
	<script>
		"use strict";
		
		var proxy = CalloutProxy.getInstance();

		function initMap() {
		var placeName = '';
		var placeCity = '';
		var lat = '';
		var lng = '';
		var myLatlng = new google.maps.LatLng(25.2854,51.5310);
		var mapProp = {
			center:myLatlng,
			zoom:12,
			mapTypeId:google.maps.MapTypeId.ROADMAP,
			streetViewControl: false,
			mapTypeControl: false,
			fullscreenControl: false
		};
		
		var map=new google.maps.Map(document.getElementById("googleMap"), mapProp);
	
			  const input = document.getElementById("pac-input");

			  const searchBox = new google.maps.places.SearchBox(input);
				map.controls[google.maps.ControlPosition.TOP_LEFT].push(input); // Bias the SearchBox results towards current map's viewport.
				
				var infoWindow = new google.maps.InfoWindow();
				const geocoder = new google.maps.Geocoder();	
				map.addListener("bounds_changed", () => {
					searchBox.setBounds(map.getBounds());
				});
				
				map.addListener('click', function(mapsMouseEvent) {
				  // Close the current InfoWindow.
					infoWindow.close();
					map.setZoom(15);
					//map.setCenter(marker.getPosition());
					console.log('mapsMouseEvent.latLng',mapsMouseEvent);
					lat = mapsMouseEvent.latLng.lat();
					lng = mapsMouseEvent.latLng.lng();
					//placeName = marker.title;
					//placeCity = marker.city;
					
					const latlng = {
						lat: parseFloat(lat),
						lng: parseFloat(lng)
					  };
					 if(mapsMouseEvent.placeId){
					 const request = {
						placeId: mapsMouseEvent.placeId,
						fields: ["name", "formatted_address", "place_id", "geometry"]
					  };
					  const service = new google.maps.places.PlacesService(map);
					  service.getDetails(request, (place, status) => {
						if (status === google.maps.places.PlacesServiceStatus.OK) {
						var placeArr = place.formatted_address.split(',');
						var city = placeArr[placeArr.length-2];
						 
					document.getElementById('city').value = city;
					document.getElementById('name').value = place.name;
					document.getElementById('lat').value = lat;
					document.getElementById('lng').value = lng;
					document.getElementById('latlng').value = lat+','+lng;
						   infoWindow = new google.maps.InfoWindow({position: mapsMouseEvent.latLng});
					 infoWindow.setContent(
							  "<div><strong>" +
								place.name +
								"</strong><br>" +
								city +
								"<br>" +
								lat +
								"<br>" +
								lng +
								"</div>"
							);
					 infoWindow.open(map);
						  
						}
					  });
					 }
				 else{
					 infoWindow = new google.maps.InfoWindow({position: mapsMouseEvent.latLng});
					 infowindow.setContent(
							  "<div>"+
								lat +
								"<br>" +
								lng +
								"</div>"
							);
					 infoWindow.open(map);
				 }
				});
				let markers = []; // Listen for the event fired when the user selects a prediction and retrieve
				// more details for that place.

				searchBox.addListener("places_changed", () => {
					const places = searchBox.getPlaces();
					 
					if (places.length == 0) {
					return;
					} // Clear out the old markers.

					markers.forEach(marker => {
					marker.setMap(null);
					});
					markers = []; // For each place, get the icon, name and location.

					const bounds = new google.maps.LatLngBounds();
					places.forEach(place => {
					if (!place.geometry) {
						console.log("Returned place contains no geometry");
						return;
					}

					const icon = {
						url: place.icon,
						size: new google.maps.Size(71, 71),
						origin: new google.maps.Point(0, 0),
						anchor: new google.maps.Point(17, 34),
						scaledSize: new google.maps.Size(25, 25)
					}; // Create a marker for each place.
					var placeAddress = place.formatted_address.split(',');
					var city = placeAddress[placeAddress.length - 2];
					console.log('place', place);
					markers.push(
						new google.maps.Marker({
						map,
						icon,
						title: place.name,
						city: city ? city : '',
						position: place.geometry.location
						})
					);

					if (place.geometry.viewport) {
						// Only geocodes have viewport.
						bounds.union(place.geometry.viewport);
					} else {
						bounds.extend(place.geometry.location);
					}
					});
					markers.forEach(marker => {
						marker.addListener("click", (mapsMouseEvent) => {
							infoWindow.close();
							map.setZoom(15);
							map.setCenter(marker.getPosition());
							lat = marker.getPosition().lat();
							lng = marker.getPosition().lng();
							placeName = marker.title;
							placeCity = marker.city;
							document.getElementById('city').value = placeCity;
							document.getElementById('name').value = placeName;
							document.getElementById('lat').value = lat;
							document.getElementById('lng').value = lng;
							document.getElementById('latlng').value = lat+','+lng;
							infoWindow = new google.maps.InfoWindow({position: mapsMouseEvent.latLng});
							infoWindow.setContent(
							  "<div><strong>" +
								placeName +
								"</strong><br>" +
								placeCity +
								"<br>" +
								lat +
								"<br>" +
								lng +
								"</div>"
							);
							infoWindow.open(map);
						});
					});
					
					map.fitBounds(bounds);
				});
				
				
		}

		const apply = () =>{
			var placeCity = document.getElementById('city').value;
			var placeName = document.getElementById('name').value;
			var lat = document.getElementById('lat').value
			var lng = document.getElementById('lng').value;
			var latlng = document.getElementById('latlng').value;
			proxy.setValue(placeCity+"&, "+placeName+"&, "+lat+"&, "+lng+"&, "+latlng);
			window.close()
		}

	</script>
	<style>
	/* Always set the map height explicitly to define the size of the div
       * element that contains the map. */
	#map {
	  height: 100%;
	}

	/* Optional: Makes the sample page fill the window. */
	html,
	body {
	  height: 100%;
	  margin: 0;
	  padding: 0;
	}
body {font-family: Arial, Helvetica, sans-serif;}
	* {box-sizing: border-box;}

	input[type=text], select, textarea {
	width: 200px;
    padding: 12px;
    border: 1px solid #ccc;
    border-radius: 4px;
    box-sizing: border-box;
    margin-top: 6px;
    margin-bottom: 16px;
    resize: vertical;
    display: inline-block;
	}

	#apply {
	  background-color: #e4e4e4;
	  color: #000;
	  padding: 12px 20px;
	  border: none;
	  border-radius: 4px;
	  cursor: pointer;
	}

	#apply:hover {
	  background-color: #aadaff;
	}
	#description {
	  font-family: Roboto;
	  font-size: 15px;
	  font-weight: 300;
	}

	#infowindow-content .title {
	  font-weight: bold;
	}

	#infowindow-content {
	  display: none;
	}

	#map #infowindow-content {
	  display: inline;
	}

	.pac-card {
	  margin: 10px 10px 0 0;
	  border-radius: 2px 0 0 2px;
	  box-sizing: border-box;
	  -moz-box-sizing: border-box;
	  outline: none;
	  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.3);
	  background-color: #fff;
	  font-family: Roboto;
	}

	#pac-container {
	  padding-bottom: 12px;
	  margin-right: 12px;
	}

	.pac-controls {
	  display: inline-block;
	  padding: 5px 11px;
	}

	.pac-controls label {
	  font-family: Roboto;
	  font-size: 13px;
	  font-weight: 300;
	}

	#pac-input {
	  background-color: #fff;
	  font-family: Roboto;
	  font-size: 15px;
	  font-weight: 300;
	  margin-left: 12px;
	  padding: 0 11px 0 13px;
	  text-overflow: ellipsis;
	  width: 400px;
	}

	#pac-input:focus {
	  border-color: #4d90fe;
	}

	#title {
	  color: #fff;
	  background-color: #4d90fe;
	  font-size: 25px;
	  font-weight: 500;
	  padding: 6px 12px;
	}

	#target {
	  width: 345px;
	}
	</style>
  </head>
  <body>
   <input
      id="pac-input"
      class="controls"
      type="text"
      placeholder="Search Box"
    />
    <div id="googleMap" style="width: 600px; height: 400px; margin-bottom: 20px"></div>
	City: <input id="city" type="text" disabled/>
	Name: <input id="name" type="text" disabled/></br>
	Lati: <input id="lat" type="text" disabled/>
	Long: <input id="lng" type="text" disabled/>
	LatLng: <input id="latlng" type="text" disabled/>
	<button id="apply" type="button" onclick="apply()">Apply</button>
  </body>
</html>
