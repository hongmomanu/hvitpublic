L.GeoIP = L.extend({

    getPosition: function (ip,callback) {
        var url = "http://freegeoip.net/json/";
        var result = L.latLng(30, 120);

        if (ip !== undefined) {
            url = url + ip;
        } else {
            //lookup our own ip address
        }
        

         $.ajax({ 
          type : "get", 
          url : url, 
          data : {}, 
          success : function(data){ 
                result.lat = data.latitude;
                result.lng = data.longitude;
                callback(result);
          } 
          }); 
        
    },

    centerMapOnPosition: function (map, ip) {
        L.GeoIP.getPosition(ip,function(position){map.setView(position);});
        

    }
});
