Vue.component('v-facets', {
  template: '<div class="facets">'
 + '	<div class="selectedFacets q-pb-md" v-if="isAnyFacetValueSelected()">'
 + '		<div v-for="(selectedFacetValues, selectedFacet) in selectedFacets" :key="selectedFacet">'
 + '			<q-chip clickable class="q-mb-sm" v-for="selectedFacetValue in selectedFacetValues" :key="selectedFacetValue.code"' 
 + '		    	@click="$emit(\'toogle-facet\', selectedFacet, selectedFacetValue, contextKey)" icon-right="cancel">{{facetLabelByCode(selectedFacet)}}: {{facetValueLabelByCode(selectedFacet, selectedFacetValue)}}'
 + '			</q-chip>'
 + '		</div>'
 + '	</div>'
 + '	<q-list v-if="facet.multiple || !isFacetSelected(facet.code)" v-for="facet in facets" :key="facet.code" class="facetValues q-py-none" dense >'
 + '		<q-item-label header><big>{{facet.label}}</big></q-item-label>'
 + '	  		<q-item v-for="(value, index) in visibleFacets(facet.code, facet.values)" :key="value.code" class="facetValue q-ml-md" clickable @click.native="$emit(\'toogle-facet\', facet.code, value.code, contextKey)">'
 + ' 				<q-item-section avatar v-if="facet.multiple" >'
 + ' 					<q-checkbox v-bind:value="isFacetValueSelected(facet.code, value.code)" :label="facetValueLabel(value.label, value.count)" @change="$emit(\'toogle-facet\', facet.code, value.code, contextKey)" ></q-checkbox>'
 + ' 				</q-item-section>'
 + '	  			<q-item-section v-else >{{value.label}}</q-item-section> <q-item-section side>{{value.count}}</q-item-section>'
 + '	  		</q-item>'
 + '			<q-btn flat v-if="facet.values.length > maxValues && !isFacetExpanded(facet.code)" @click="expandFacet(facet.code)">{{$q.lang.vui.facets.showAll}}</q-btn>'
 + '			<q-btn flat v-if="facet.values.length > maxValues && isFacetExpanded(facet.code)" @click="reduceFacet(facet.code)">{{$q.lang.vui.facets.showLess}}</q-btn>'
 + '	</q-list>'
 + '</div>'
  ,
  props: {
    facets: Array,
    selectedFacets: Object,
    contextKey: String,
    maxValues: {
    	type : Number,
    	default : 5
    }
  },
  computed: {
  },
  data : function() {
	  return {
		  expandedFacets:[]
	  }
  },
  methods: {
	  facetByCode : function (facetCode) {
		  return this.facets.filter(function (facet) {
			  return facet.code === facetCode;
		  })[0];
	  },
	  facetLabelByCode : function (facetCode) {
		  return this.facetByCode(facetCode).label;
	  },
	  facetValueLabelByCode : function (facetCode, facetValueCode) {
		  return this.facetByCode(facetCode).values.filter(function (facetValue) {
			  return facetValue.code === facetValueCode;
		  })[0].label;
	  },
	  isAnyFacetValueSelected : function() {
		  for(var facetCode in this.selectedFacets) {
			  if(this.selectedFacets[facetCode].length > 0) {
				  return true;
			  }
		  } 
		  return false;
	  },
	  isFacetValueSelected : function (facetCode, facetValueCode){
		  return this.selectedFacets[facetCode].includes(facetValueCode);
	  },
	  isFacetSelected : function (facetCode){
		  return this.selectedFacets[facetCode].length > 0;
	  },
	  facetValueLabel : function (label, count){
		  return label + ' (' + count + ')';
	  },
	  expandFacet : function (facetCode){
		  if (!this.isFacetExpanded(facetCode)) {
			  this.$data.expandedFacets.push(facetCode);
		  }
	  },
	  reduceFacet : function (facetCode){
		  if (this.isFacetExpanded(facetCode)) {
			  this.$data.expandedFacets.splice( this.$data.expandedFacets.indexOf(facetCode), 1);
		  }
	  },
	  isFacetExpanded : function (facetCode){
		 return this.$data.expandedFacets.includes(facetCode);
	  },
	  visibleFacets : function (facetCode, facetValues){
		  if (!this.isFacetExpanded(facetCode)) {
			  return  facetValues.slice(0, this.maxValues);
		  }
		  return facetValues;
	  }
  }
})

