var map; // the map object
var layers = {};
var wms_layers = [];
var drawControl;
var display_layers=[];
var proxy="../auth/proxy?url="

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
                var layertype=$.evalJSON(resbase[i].imgcss);
                if(layertype.type==='tile'){
                    baseMaps[resbase[i].text]=L.tileLayer(resbase[i].value, {
                        minZoom: layertype.level[0],
                        maxZoom: layertype.level[1]
                    });
                }

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
                        var layertype=$.evalJSON(res[i].imgcss);

                        if(layertype.type==='tile'){
                            overlayMaps[res[i].text]=L.tileLayer(res[i].value, {
                                minZoom: layertype.level[0],
                                maxZoom: layertype.level[1]
                            });
                        }
                        else if(layertype.type==='wms'){
                            var wms_layer=L.tileLayer.wms(res[i].value+"?service=wms", {
                                layers: layertype.layers,
                                format: 'image/png',
                                transparent: true,
                                crs:eval(layertype.crs),
                                //attribution: "CDC",
                                noWrap: true
                            });
                            overlayMaps[res[i].text]=wms_layer;
                            wms_layers.push(
                                {
                                    text:res[i].text,
                                    value:res[i].value,
                                    layers:layertype.layers,
                                    searchField:layertype.searchField,
                                    propertyName:layertype.propertyName,
                                    zoom:layertype.zoom
                                });
                        }

                        if(i===0)display_layers.push(overlayMaps[res[i].text]);
                    }
                    var layersControl = new L.Control.Layers(baseMaps, overlayMaps);
                    if(display_layers.length===0)alert("无地图资源");
                    map =new L.Map('map', {center:[30,120], zoom: 9,
                        layers: display_layers});

                    map.addControl(layersControl);

                    $.ajax({
                        type: 'get',
                        dataType: 'json',
                        url: '../auth/getfuncsbyrole',
                        data: {type:'图层编辑'},
                        success:function(res){
                            var editLayers=[];
                            for(var i=0;i<res.length;i++){
                                editLayers.push(res[i].value);
                            }
                            map.addControl( new L.Control.Search(
                                {
                                    searchLayers:wms_layers,
                                    editLayers:editLayers
                                })
                            );

                        }
                    });



                    //L.easyButton( "fa-edit" , function(){alert(2)} , "编辑" )

                    /*// Initialize the WFST layer
                    layers.drawnItems = L.wfst(null,{
                        // Required
                        url : proxy+'http://192.168.2.141:8082/geoserver/zs_csmz/wfs', //'http://192.168.2.142:8080/geoserver/zsmz/wfs'
                        featureNS : 'zs_csmz',//xsdata  zs_csmz
                        version:'1.1.0',
                        featureType : 'STR_XianJ'*//*,STP_DW STL_ALL_ROAD  STR_XianJ
                        primaryKeyField: 'id'*//*
                    }).addTo(map);

                    // Initialize the draw control and pass it the FeatureGroup of editable layers
                    var drawControl = new L.Control.Draw({
                        edit: {
                            featureGroup: layers.drawnItems
                        }
                    });

                    map.addControl(drawControl);

                    map.on('draw:created', function (e) {
                        layers.drawnItems.addLayer(e.layer,{"success":function(){}});
                    });
                    map.on('draw:editstart', function (e) {
                        //layers.drawnItems.addLayer(e.layer);
                    });
                    map.on('draw:edited', function (e) {

                        layers.drawnItems.wfstSave(layers.drawnItems);
                    });*/

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
