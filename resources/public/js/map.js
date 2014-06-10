var map; // the map object
var layers = {};
var drawControl;
var display_layers=[];

function initMap(){
    // create a map in the "map" div, set the view to a given place and zoom


    $.ajax({
        type: 'get',
        dataType: 'json',
        url: '../auth/getfuncsbyrole',
        data: {type:'底图'},
        //complete :compfunc,
        //error:errorfunc,
        success: function(resbase){
            var baseMaps={};
            for(var i=0;i<resbase.length;i++){
                baseMaps[resbase[i].text]=L.tileLayer(resbase[i].value, {
                    minZoom: 4,
                    maxZoom: 18
                });
                if(i===0)display_layers.push(baseMaps[resbase[i].text]);

            }
            $.ajax({
                type: 'get',
                dataType: 'json',
                url: '../auth/getfuncsbyrole',
                data: {type:'覆盖图'},
                success:function(res){
                   var overlayMaps={};
                    for(var i=0;i<res.length;i++){
                        overlayMaps[res[i].text]=L.tileLayer(res[i].value, {
                            minZoom: 4,
                            maxZoom: 18
                        });

                        if(i===0)display_layers.push(overlayMaps[res[i].text]);
                    }
                    var layersControl = new L.Control.Layers(baseMaps, overlayMaps);
                    if(display_layers.length===0)alert("无地图资源");
                    map =new  L.Map('map', {center: [30.258 , 120.1556], zoom: 12,
                        layers: display_layers});

                    map.addControl(layersControl);
                    //console.log(L.GeoIP.getPosition());

                    L.GeoIP.centerMapOnPosition(map);


                }
            });

        }
    });





    // Set the map background to our WMS layer of the world boundaries
    // Replace this with your own background layer
    /*layers.world = L.tileLayer.wms("/geoserver/wfsttest/wms", {
        layers: 'wfsttest:world',
        format: 'image/png',
        transparent: true,
        attribution: "CDC",
        noWrap: true
    }).addTo(map);

    // Initialize the WFST layer 
    layers.drawnItems = L.wfst(null,{
        // Required
        url : '/geoserver/wfsttest/wfs',
        featureNS : 'wfsttest',
        featureType : 'doodles',
        primaryKeyField: 'id'
    }).addTo(map);

    // Initialize the draw control and pass it the FeatureGroup of editable layers
    var drawControl = new L.Control.Draw({
        edit: {
            featureGroup: layers.drawnItems
        }
    });

    map.addControl(drawControl);

    map.on('draw:created', function (e) {
        layers.drawnItems.addLayer(e.layer);
    });
    map.on('draw:edited', function (e) {
        layers.drawnItems.wfstSave(e.layers);
    });*/
}