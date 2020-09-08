import Quasar from "quasar"
import {sortDate } from "quasar/src/utils/sort.js"
import {isNumber, isDate} from "quasar/src/utils/is.js"

export default {
    onAjaxError : function(response) {
        //Quasar Notif Schema
        let notif = {
            type: 'negative',
            message: 'Network Error.',
            icon: 'warning',
            timeout: 2500,
        }
        
        //Setup Error Message //if response was an error
        if(Object.prototype.hasOwnProperty.call(response,'message')){ 
            notif.message = response.message
        }
        //Setup Generic Response Messages
        if(response.status === 401){
            notif.message = 'UnAuthorized, you may login with an authorized account'
            this.$root.$emit('logout') //Emit Logout Event
        } else if(response.status === 403){
            notif.message = 'Forbidden, your havn&quote;t enought rights'					 
        } else if(response.status === 404){
            notif.message = 'API Route is Missing or Undefined'
        } else if(response.status === 405){
            notif.message = 'API Route Method Not Allowed'
        } else if(response.status === 422){
            //Validation Message
            notif.message = '';
            Object.keys(response.body).forEach(function (key) {
            this.$data.uiMessageStack[key] = response.body[key];
            }.bind(this));
        } else if(response.status >= 500){
            notif.message = 'Server Error'
        }
        if(response.statusText) { 
            notif.message = response.statusText
        }
        //Try to Use the Response Message
        if(Object.prototype.hasOwnProperty.call(response,'data')){
            if(Object.prototype.hasOwnProperty.call(response.data, 'message') && response.data.message && response.data.message.length > 0){
                notif.message = response.data.message
            } else if(Object.prototype.hasOwnProperty.call(response.data, 'globalErrors') && response.data.globalErrors && response.data.globalErrors.length > 0){
                notif.message = response.data.globalErrors.join('<br/>\n ');
            }
        }
        //Send the notif
        if(notif.message.length > 0) {
            this.$q.notify(notif);
        }
    },

    getSafeValue : function(objectkey, fieldKey, subFieldKey) {
        if (this.$data.vueData[objectkey] && this.$data.vueData[objectkey][fieldKey] ) {
            return this.$data.vueData[objectkey][fieldKey][subFieldKey];
        } 
        return null;
    },

    transformListForSelection : function (list, valueField, labelField) {
        return this.$data.vueData[list].map(function (object) {
            return { value: object[valueField], label: object[labelField].toString()} // a label is always a string
        });
    },

    paginationAndSortHandler : function (params) {
        var pagination = params.pagination;
        let componentStates = this.$data.componentStates;
        let vueData = this.$data.vueData;
        var oldPagination = componentStates[pagination.componentId].pagination;
        if (oldPagination.sortBy != pagination.sortBy ||  oldPagination.descending != pagination.descending ) {
            if (pagination.sortBy) {
                // it's a sort
                if (pagination.sortUrl) {
                    //order call the server
                    pagination.page = 1 //reset pagination
                    this.$http.post(pagination.sortUrl, { sortFieldName : pagination.sortBy, sortDesc : pagination.descending, CTX: this.$data.vueData.CTX}, { emulateJSON: true })
                    .then( function (response ) {
                        vueData[pagination.listKey] = response.body.model[pagination.listKey];
                        this.$data.vueData.CTX = response.body.model['CTX'];
                    });
                } else {
                    //do locally
                    this.$refs[pagination.componentId].sortMethod.apply(this.$refs[pagination.componentId], [vueData[pagination.listKey], pagination.sortBy, pagination.descending])
                }
            } // if we reset the sort we do nothing
        }
            // otherwise it's pagination or filter : do it locally
            // nothing to do everything is done by the paginatedData function
        componentStates[pagination.componentId].pagination = pagination;
    },
    paginatedData: function (list, componentId) {
        let componentStates = this.$data.componentStates;
        var pagination = componentStates[componentId].pagination;
        if (pagination.rowsPerPage != 0) { // not all
            var firstRowIndex = (pagination.page - 1) * pagination.rowsPerPage;
            var lastRowIndex = pagination.page * pagination.rowsPerPage;
            return this.$data.vueData[list].slice(firstRowIndex, lastRowIndex);
        }
        return this.$data.vueData[list];
    },


    sortCiAi: function (data, sortBy, descending) {
        const col = this.colList.find(def => def.name === sortBy)
        if (col === void 0 || col.field === void 0) {
          return data
        }

        const
          dir = descending === true ? -1 : 1,
          val = typeof col.field === 'function'
            ? v => col.field(v)
            : v => v[col.field]

        const collator = new Intl.Collator();

        return data.sort((a, b) => {
            let	A = val(a),
                B = val(b)

            if (A === null || A === void 0) {
                return -1 * dir;
            }
            if (B === null || B === void 0) {
                return 1 * dir;
            }
            if (col.sort !== void 0) {
                return col.sort(A, B, a, b) * dir;
            }
            if (isNumber(A) === true && isNumber(B) === true) {
                return (A - B) * dir;
            }
            if (isDate(A) === true && isDate(B) === true) {
                return sortDate(A, B) * dir;
            }
            if (typeof A === 'boolean' && typeof B === 'boolean') {
                return (A - B) * dir;
            }

            [A, B] = [A, B].map(s => (s + '').toLocaleString());

            return collator.compare(A, B) * dir;
        })
    },

    selectedFunction : function (object, field, item, /*keyboard*/) {
        this.$data.vueData[object][field] = item.value;
    },

    searchAutocomplete : function (list, valueField, labelField, componentId, url, terms, update, abort) {
        if (terms.length < 2) {
            abort();
            return
        }
        this.$http.post(url, {terms: terms, list : list , valueField : valueField,labelField : labelField, CTX: this.$data.vueData.CTX}, { emulateJSON: true }).then( function (response ) {
            var finalList =  response.body.map(function (object) {
                return { value: object[valueField], label: object[labelField].toString()} // a label is always a string
            });
            update(function() {
                this.$data.componentStates[componentId].options = finalList;
            }.bind(this));
        }, 
        function (response) {
            this.$q.notify(response.status + ":" +response.statusText);
            update([]);
        });
        
    },

    decodeDate : function (object, field, format) {
        var value = this.$data.vueData[object][field];
        if(value === Quasar.utils.date.formatDate(Quasar.utils.date.extractDate(value, 'DD/MM/YYYY'), 'DD/MM/YYYY')) {
            return Quasar.utils.date.formatDate(Quasar.utils.date.extractDate(value, 'DD/MM/YYYY'), format);
        } else {
            return value;
        }
    },

    encodeDate : function (object, field, newValue, format) {
        if(newValue === Quasar.utils.date.formatDate(Quasar.utils.date.extractDate(newValue, format ), format)) {
            this.$data.vueData[object][field] = Quasar.utils.date.formatDate(Quasar.utils.date.extractDate(newValue, format ), 'DD/MM/YYYY');
        } else {
            this.$data.vueData[object][field] = newValue;
        }
    },

    decodeDatetime : function (object, field, format) {
        var value = this.$data.vueData[object][field];
        if(value === Quasar.utils.date.formatDate(Quasar.utils.date.extractDate(value, 'DD/MM/YYYY HH:mm'), 'DD/MM/YYYY HH:mm')) {
            return Quasar.utils.date.formatDate(Quasar.utils.date.extractDate(value, 'DD/MM/YYYY HH:mm'), format);
        } else {
            return value;
        }
    },

    encodeDatetime : function (object, field, newValue, format) {
        if(newValue === Quasar.utils.date.formatDate(Quasar.utils.date.extractDate(newValue, format ), format)) {
            this.$data.vueData[object][field] = Quasar.utils.date.formatDate(Quasar.utils.date.extractDate(newValue, format ), 'DD/MM/YYYY HH:mm');
        } else {
            this.$data.vueData[object][field] = newValue;
        }
    },

    sortDatesAsString : function (format) {
        return function (date1, date2) {
            return (Quasar.utils.date.extractDate(date1, format).getTime() > Quasar.utils.date.extractDate(date2, format).getTime()) ? 1 : -1;
        }
    },

    goTo : function (url) {
        window.location = url;
    },

    openModal : function (modalId, url, params) {
        if (url) {
            var finalUrl = url;
            if (params && Object.keys(params).length > 0) {
                var paramsString = Object.keys(params).map(function(key){
                    return key + '=' + params[key] ;
                }).join("&");
                finalUrl = finalUrl + '?' + paramsString;
            }
            this.$data.componentStates[modalId].srcUrl = finalUrl;
        }
        this.$data.componentStates[modalId].opened = true;
    },

    toogleFacet : function (facetCode, facetValueCode, contextKey) {
        let vueData = this.$data.vueData;
        var multiple = false;
        vueData[contextKey+"_facets"].forEach(function (facet) {
            if (facet.code === facetCode) {
                // get the right facet 
                multiple = facet.multiple;
            }
        })
        var selectedFacetValues = vueData[contextKey+"_selectedFacets"][facetCode]
        if (selectedFacetValues) {
            if (selectedFacetValues.includes(facetValueCode)) {
                if (multiple) {
                    selectedFacetValues.splice(selectedFacetValues.indexOf(facetValueCode), 1);
                } else {
                    selectedFacetValues.splice(0);
                }
            } else {
                selectedFacetValues.push(facetValueCode);
            }
        } else {
            vueData[contextKey+"_selectedFacets"][facetCode] = [facetValueCode];
        }
        this.search(contextKey);
    },

    search : Quasar.utils.debounce(function(contextKey) { 
        let componentStates = this.$data.componentStates;
        let vueData = this.$data.vueData;
        var selectedFacetsContextKey = contextKey +"_selectedFacets";
        var criteriaContextKey = vueData[contextKey + '_criteriaContextKey'];
        var params = this.vueDataParams([criteriaContextKey]);
        params['selectedFacets'] = JSON.stringify(vueData[selectedFacetsContextKey]);
        
        var searchUrl = componentStates[contextKey+'Search'].searchUrl;
        var collectionComponentId = componentStates[contextKey+'Search'].collectionComponentId;
        
        if (componentStates[collectionComponentId].pagination && componentStates[collectionComponentId].pagination.sortBy) {
            var collectionPagination = componentStates[collectionComponentId].pagination;
            params['sortFieldName'] = collectionPagination.sortBy;
            params['sortDesc'] = collectionPagination.descending;
        }
        this.httpPostAjax(searchUrl, params, {  
            onSuccess : function (response ) {
                if (componentStates[collectionComponentId].pagination) {
                    var collectionPagination = componentStates[collectionComponentId].pagination;
                    collectionPagination.page = 1 // reset page
                    collectionPagination.rowsNumber = response.body.model[contextKey+'_list'].length
                }
            }});
    }, 400),

    showMore : function (componentId) {
        let componentStates = this.$data.componentStates;
        var showMoreCount = componentStates[componentId].pagination.showMoreCount;
        if (showMoreCount) {
            // nothing to do
        } else {
            showMoreCount = 1;
            componentStates[componentId].pagination.showMoreCount = showMoreCount;
        }
        componentStates[componentId].pagination.rowsPerPage = componentStates[componentId].pagination.rowsPerPage / showMoreCount * (showMoreCount + 1);
    },

    uploader_addedFile : function(isMultiple, componentId) {
        let componentStates = this.$data.componentStates;
        if (!isMultiple) {
            this.$refs[componentId].removeUploadedFiles();
            componentStates[componentId].fileUris = [];
        }
    },

    uploader_uploadedFiles : function(uploadInfo, componentId) {
        let componentStates = this.$data.componentStates;
        uploadInfo.files.forEach(function(file) {
            componentStates[componentId].fileUris.push(file.xhr.response);
            file.fileUri = file.xhr.response;
        });
    },

    uploader_removeFiles : function(removedFiles, componentId) {
        let componentStates = this.$data.componentStates;
        removedFiles.forEach(function(removedFile) {
            var component = this.$refs[componentId];
            var componentFileUris = componentStates[componentId].fileUris;
            var indexOfFileUri = componentFileUris.indexOf(removedFile.fileUri);
            var xhrParams = {};
            xhrParams[component.fieldName] = removedFile.fileUri;
            this.$http.delete(component.url, {params : xhrParams, credentials:component.withCredentials})
            .then( function (/*response*/) { //Ok
                if (component.multiple) {
                    componentFileUris.splice(indexOfFileUri, 1);
                } else {
                    componentFileUris.splice(0);
                }
            }, function(response) { //Ko
                this.$q.notify(response.status + ":" +response.statusText+ " Can't remove temporary file");
            });
        }.bind(this));
    },

    httpPostAjax : function(url, params, options) {
        let vueData = this.$data.vueData;
        let uiMessageStack = this.$data.uiMessageStack;
        params['CTX'] = vueData.CTX;
        this.$http.post(url, params , { emulateJSON: true }).then( function (response ) {
            if (response.body.model.CTX) {
                vueData.CTX = response.body.model.CTX;
            }
            Object.keys(response.body.model).forEach(function (key) {
                if ('CTX' != key) {
                    vueData[key] = response.body.model[key];
                }
            });
            Object.keys(response.body.uiMessageStack).forEach(function (key) {
                uiMessageStack[key] = response.body.uiMessageStack[key];
            });
            if (options && options.onSuccess) {
                options.onSuccess(response);
            }
        }.bind(this)).catch(function (response) {
            if (options && options.onError) {
                options.onError(response.error);
            }
        })
        ;
    },

    hasFieldsError: function (object, field) {
        var fieldsErrors = this.$data.uiMessageStack.objectFieldErrors;
        return fieldsErrors &&  Object.prototype.hasOwnProperty.call(fieldsErrors, object) &&
        fieldsErrors[object] && Object.prototype.hasOwnProperty.call(fieldsErrors[object], field) && fieldsErrors[object][field].length > 0;
    },

    getErrorMessage: function (object, field) {
        var fieldsErrors = this.$data.uiMessageStack.objectFieldErrors;
        if (fieldsErrors && Object.prototype.hasOwnProperty.call(fieldsErrors, object) &&
            fieldsErrors[object] && Object.prototype.hasOwnProperty.call(fieldsErrors[object], field)) {
            return fieldsErrors[object][field].toString(); 
        } else { 
            return '';
        }
    },

    vueDataParams: function (keys) {
        var params = {};
        for (var i = 0; i < keys.length; i++) {
            var contextKey = keys[i];
            var vueDataValue = this.$data.vueData[contextKey];
            if (vueDataValue && typeof vueDataValue === 'object' && Array.isArray(vueDataValue) === false) {
                // object
                Object.keys(vueDataValue).forEach(function (propertyKey) {
                    if(!propertyKey.startsWith("_")) {
                        // _ properties are private and don't belong to the serialized entity
                        if (Array.isArray(vueDataValue[propertyKey])) {
                            vueDataValue[propertyKey].forEach(function (value, index) {
                                if(vueDataValue[propertyKey][index] && typeof vueDataValue[propertyKey][index] === 'object' ) {
                                    params['vContext['+contextKey+']['+propertyKey+']['+index+']'] = vueDataValue[propertyKey][index]['_v_inputValue'];
                                } else {
                                    params['vContext['+contextKey+']['+propertyKey+']['+index+']'] = vueDataValue[propertyKey][index];
                                }
                            });
                        } else {
                            if(vueDataValue[propertyKey] && typeof vueDataValue[propertyKey] === 'object' ) {
                                params['vContext['+contextKey+']['+propertyKey+']'] = vueDataValue[propertyKey]['_v_inputValue'];
                            } else {
                                params['vContext['+contextKey+']['+propertyKey+']'] = vueDataValue[propertyKey];
                            }
                        }
                    }
                });
            } else {
                //primitive
                params['vContext['+contextKey+']'] = vueDataValue;
            }
        }
        return params;
        
    }

}