(function() {

L.Control.Edit = L.Control.extend({
	includes: L.Mixin.Events,

	options: {
					//autoresize on input change
		collapsed: true,			//collapse search control at startup
		autoCollapse: false,		//collapse search control after submit(on button or on tips if enabled tipAutoSubmit)
		//TODO add option for persist markerLoc after collapse!
		autoCollapseTime: 1200,		//delay for autoclosing alert and collapse after blur
		zoom: null,					//zoom after pan to location found, default: map.getZoom()

		position: 'topleft',
        editLayers:[],
        searchField:'',
		animateLocation: true,		//animate a circle over location found
		markerIcon: new L.Icon.Default()//custom icon for maker location
	},

	initialize: function(options) {
		L.Util.setOptions(this, options || {});
		this._countertips = 0;		//number of tips items
		this._recordsCache = {};	//key,value table! that store locations! format: key,latlng
        this._editLayers=this.options.editLayers;
        this._selectEditLayer=null;

	},

	onAdd: function (map) {
		this._map = map;
		this._container = L.DomUtil.create('div', 'leaflet-control-search');
        this._searchDiv=this._createSearchDiv(this.options.text,'leaflet-search-div');

        this._select = this._createLayersSelect(this.options.text, 'easyui-combobox');

		this._button = this._createButton(this.options.text, 'search-button');

		if(this.options.collapsed===false)
			this.expand();


		 map.on({
		// 		'layeradd': this._onLayerAddRemove,
		// 		'layerremove': this._onLayerAddRemove
		     /*'resize': this._handleAutoresize*/
		 	}, this);
		return this._container;
	},
	addTo: function (map) {

		if(this.options.wrapper) {
			this._container = this.onAdd(map);
			this._wrapper = L.DomUtil.get(this.options.wrapper);
			this._wrapper.style.position = 'relative';
			this._wrapper.appendChild(this._container);
		}
		else
			L.Control.prototype.addTo.call(this, map);

        var me=this;
        easyloader.load('combobox',function(){
            $("#map_search_layers").combobox({
                onSelect: function(rec){
                    me._selectSearchLayer=rec;
                }
            });
        });


		return this;
	},

	onRemove: function(map) {
		this._recordsCache = {};
		// map.off({
		// 		'layeradd': this._onLayerAddRemove,
		// 		'layerremove': this._onLayerAddRemove
		// 	}, this);
	},

	// _onLayerAddRemove: function(e) {
	// 	//console.info('_onLayerAddRemove');
	// 	//without this, run setLayer also for each Markers!! to optimize!
	// 	if(e.layer instanceof L.LayerGroup)
	// 		if( L.stamp(e.layer) != L.stamp(this._layer) )
	// 			this.setLayer(e.layer);
	// },


	expand: function() {
        $(this._searchDiv).show()
		L.DomUtil.addClass(this._container, 'search-exp');
		this._map.on('dragstart click', this.collapse, this);
		return this;	
	},

	collapse: function() {

		if(this.options.collapsed)
		{
            $(this._searchDiv).hide();
			L.DomUtil.removeClass(this._container, 'search-exp');
			this._map.off('dragstart click', this.collapse, this);
		}
		this.fire('search_collapsed');
		return this;
	},
	
	collapseDelayed: function() {	//collapse after delay, used on_input blur
		if (!this.options.autoCollapse) return this;
		var that = this;
		clearTimeout(this.timerCollapse);
		this.timerCollapse = setTimeout(function() {
			that.collapse();
		}, this.options.autoCollapseTime);
		return this;		
	},

	collapseDelayedStop: function() {
		clearTimeout(this.timerCollapse);
		return this;		
	},

    _createSearchDiv:function(text,className){
        var div=L.DomUtil.create('div', className,this._container);
        div.style.display = 'none';
        L.DomEvent
            .disableClickPropagation(div)
            //.on(input, 'keyup', this._handleKeypress, this)
            //.on(input, 'keydown', this._handleAutoresize, this)
            .on(div, 'blur', this.collapseDelayed, this)
            .on(div, 'focus', this.collapseDelayedStop, this);
        return div;
    },
    _createLayersSelect: function (text, className) {
		var input = L.DomUtil.create('input', className, this._searchDiv); //this._container

        $(input).attr("id","map_search_layers");
        $(input).attr("data-options",
                "data:"+ $.toJSON(this._searchLayers)
                +""
        );

		L.DomEvent
			.disableClickPropagation(input)
			//.on(input, 'keyup', this._handleKeypress, this)
			//.on(input, 'keydown', this._handleAutoresize, this)
			.on(input, 'blur', this.collapseDelayed, this)
			.on(input, 'focus', this.collapseDelayedStop, this);

		return input;
	},



	_createButton: function (title, className) {
		var button = L.DomUtil.create('a', className, this._container);
		button.href = '#';
		button.title = title;

		L.DomEvent
			.on(button, 'click', L.DomEvent.stop, this)
			.on(button, 'click', this._handleSubmit, this)			
			.on(button, 'focus', this.collapseDelayedStop, this)
			.on(button, 'blur', this.collapseDelayed, this);

		return button;
	},


	
	_handleKeypress: function (e) {	//run _input keyup event
		
		switch(e.keyCode)
		{
			case 27: //Esc
				this.collapse();
			break;
			case 13: //Enter
				if(this._countertips == 1)
					this._handleArrowSelect(1);
				this._handleSubmit();	//do search
			break;
			case 38://Up
				this._handleArrowSelect(-1);
			break;
			case 40://Down
				this._handleArrowSelect(1);
			break;
			case 37://Left
			case 39://Right
			case 16://Shift
			case 17://Ctrl
			//case 32://Space
			break;
			case 8://backspace
			case 46://delete
				this._autoTypeTmp = false;//disable temporarily autoType
			break;
			default://All keys

				if(this._input.value.length)
					this._cancel.style.display = 'inline-block';
				else
					this._cancel.style.display = 'none';

				if(this._input.value.length >= this.options.minLength)
				{

                    var that = this;
					clearTimeout(this.timerKeypress);	//cancel last search request while type in				
					this.timerKeypress = setTimeout(function() {	//delay before request, for limit jsonp/ajax request

						that._fillRecordsCache();
					
					}, this.options.delayType);
				}
				else
					this._hideTooltip();
		}
	},

	_handleSubmit: function() {	//button and tooltip click and enter submit



		if(this._searchDiv.style.display == 'none')	//on first click show _input only
			this.expand();
		else
		{
		    this.collapse();
		}
	}


});


L.Map.addInitHook(function () {
    if (this.options.editControl) {
        this.editControl = L.control.edit(this.options.editControl);
        this.addControl(this.editControl);
    }
});

L.control.edit = function (options) {
    return new L.Control.Edit(options);
};

}).call(this);

