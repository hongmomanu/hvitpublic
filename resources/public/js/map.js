var map; // the map object
var layers = {};
var wms_layers = [];
var drawControl;
var display_layers=[];
var proxy="../auth/proxy?url="

function initMap(){
    // create a map in the "map" div, set the view to a given place and zoom

    function makeDisplayLayer(text,defualtlayers,layer){
        for(var i=0;i<defualtlayers.length;i++){
         
            if(defualtlayers[i].value===text){
                
                display_layers.push(layer);
                break;
            }
        }
    }     
    $.ajax({
        type: 'get',
        dataType: 'json',
        url: '../auth/getfuncsbyrole',
        data: {type:'底图,覆盖图,图层编辑,默认加载图层'},
        //complete :compfunc,
        //error:errorfunc,
        success: function(resbase){
            var baseMaps={};
            var overlayMaps={};
            var editLayers=[];
            var defaultLayers=resbase['默认加载图层'];

            for(var i=0;i<resbase['底图'].length;i++){
                var layertype=$.evalJSON(resbase['底图'][i].imgcss);
                if(layertype.type==='tile'){
                    var layer=L.tileLayer(resbase['底图'][i].value, {
                        minZoom: layertype.level[0],
                        maxZoom: layertype.level[1]
                    });
                    baseMaps[resbase['底图'][i].text]=layer;
                    makeDisplayLayer(resbase['底图'][i].text,defaultLayers,layer);
                }

            }
            for(var i=0;i<resbase['覆盖图'].length;i++){
                var layertype=$.evalJSON(resbase['覆盖图'][i].imgcss);

                if(layertype.type==='tile'){
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
                    makeDisplayLayer(resbase['覆盖图'][i].text,defaultLayers,layer);
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
            var layersControl = new L.Control.Layers(baseMaps, overlayMaps);
            if(display_layers.length===0)alert("无地图资源");
            map =new L.Map('map', {center:[30,120], zoom: 9,
                layers: display_layers});
            L.GeoIP.centerMapOnPosition(map);

            map.addControl(layersControl);


            for(var i=0;i<resbase['图层编辑'].length;i++){
                editLayers.push(resbase['图层编辑'][i].value);
            }
            map.addControl( new L.Control.Search(
            {
                searchLayers:wms_layers,
                editLayers:editLayers
            })
            );

        }
    });

}
