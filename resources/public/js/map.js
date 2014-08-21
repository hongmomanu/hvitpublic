var map; // the map object
var layers = {};
var wms_layers = [];
var drawControl;
var display_layers=[];
var proxy="../auth/proxy?url=";
var self=null;


function initMap(){
    // create a map in the "map" div, set the view to a given place and zoom
    function makeDisplayLayer(text,defualtlayers,layer){
        for(var i=0;i<defualtlayers.length;i++){

            if(defualtlayers[i].value===text){

                display_layers.push(layer);
                return true;
            }
        }
        return false;
    }     
    $.ajax({
        type: 'get',
        dataType: 'json',
        url: '../auth/getfuncsbyrole',
        data: {type:'底图,覆盖图,图层编辑,默认加载图层,地图插件'},
        //complete :compfunc,
        //error:errorfunc,
        success: function(resbase){
            var baseMaps={};
            var overlayMaps={};
            var editLayers=[];
            var defaultLayers=resbase['默认加载图层'];
            var plugins=resbase['地图插件'];
            var miniLayer=null;

            //init base layer
            for(var i=0;i<resbase['底图'].length;i++){
                var layertype=$.evalJSON(resbase['底图'][i].imgcss);
                if(layertype.type==='tile'){
                    var options={
                        minZoom: layertype.level[0],
                        maxZoom: layertype.level[1]
                    };
                    var layer=L.tileLayer(resbase['底图'][i].value, options);
                    baseMaps[resbase['底图'][i].text]=layer;
                    var isDisplay=makeDisplayLayer(resbase['底图'][i].text,defaultLayers,layer);
                    if(isDisplay)miniLayer=L.tileLayer(resbase['底图'][i].value, options);
                }

            }
            //init over layer
            for(var i=0;i<resbase['覆盖图'].length;i++){
                var layertype=$.evalJSON(resbase['覆盖图'][i].imgcss);

                if( layertype.type==='tile'){
                    var layer=L.tileLayer(resbase['覆盖图'][i].value, {
                        minZoom: layertype.level[0],
                        maxZoom: layertype.level[1]
                    });
                    overlayMaps[resbase['覆盖图'][i].text]=layer;
                    makeDisplayLayer(resbase['覆盖图'][i].text,defaultLayers,layer);
                }
                else if(layertype.type==='wms'){
                    var wms_layer=L.tileLayer.wms(resbase['覆盖图'][i].value+"?service=wms", {
                        layers: layertype.layers,
                        format: 'image/png',
                        transparent: true,
                        crs:eval(layertype.crs),
                                //attribution: "CDC",
                                noWrap: true
                            });
                    overlayMaps[resbase['覆盖图'][i].text]=wms_layer;
                    makeDisplayLayer(resbase['覆盖图'][i].text,defaultLayers,wms_layer);
                    wms_layers.push(
                    {
                        text:resbase['覆盖图'][i].text,
                        value:resbase['覆盖图'][i].value,
                        layers:layertype.layers,
                        searchField:layertype.searchField,
                        propertyName:layertype.propertyName,
                        shape:layertype.shape,
                        zoom:layertype.zoom
                    });
                }


            }

            // init plugins
            var hasminimap=false;
            var hasfullmap=false;
            for(var i=0;i<plugins.length;i++){
                if(plugins[i].text=="小地图"){
                    hasminimap=true;
                }
                else if(plugins[i].text=="地图全屏"){
                    hasfullmap=true;
                }
            }

            // init basemap 
            var layersControl = new L.Control.Layers(baseMaps, overlayMaps);
            if(display_layers.length===0)alert("无地图资源");
            map =new L.Map('map', {
                center:[30,120], 
                zoom: 9,
                fullscreenControl: hasfullmap,
                fullscreenControlOptions: {
                    position: 'topleft'
                },
                layers: display_layers
            });
            L.GeoIP.centerMapOnPosition(map);
            map.addControl(layersControl);

            //load search layers
            for(var i=0;i<resbase['图层编辑'].length;i++){
                editLayers.push(resbase['图层编辑'][i].value);
            }
            map.addControl( new L.Control.Search(
            {
                searchLayers:wms_layers,
                editLayers:editLayers
            })
            );
            
               //load plugins
               if(hasminimap){
                var miniMap = new L.Control.MiniMap(miniLayer, { toggleDisplay: true }).addTo(map);
                map.on('baselayerchange',function(e){
                    map.removeControl(miniMap);
                    miniLayer=new L.TileLayer(e.layer._url,e.layer.options);
                    miniMap = new L.Control.MiniMap(miniLayer, { toggleDisplay: true }).addTo(map);
                });
            }

        }
    });

}
