import Vue from "vue"
import Quasar from "quasar"

export default {
    bind: function(elMaxi, args) {
            const topOffset = args.value.topOffset;
            const topOffsetElSelector = args.value.topOffsetEl;
            const leftOffset = args.value.leftOffset;
            const leftOffsetElSelector = args.value.leftOffsetEl;
            const elMini = elMaxi.querySelector('.mini')
            for(var i=0 ; i<elMaxi.childNodes.length; i++) {
                var elChild = elMaxi.childNodes[i];
                if(elChild.classList && !elChild.classList.contains('mini')) {
                    elChild.classList.add("not-mini")
                }
            }
            
            Vue.minifyHandler = function() {
                var currentTopOffset = Vue.minifyComputeOffset(topOffset, topOffsetElSelector, 0, 'TOP');
                var currentLeftOffset = Vue.minifyComputeOffset(leftOffset, leftOffsetElSelector, 0, 'LEFT');
                
                var elMiniHeight = elMini.getBoundingClientRect().height-currentTopOffset;
                var elMaxiHeight = elMaxi.getBoundingClientRect().height;
                //We check if nav should be fixed
                // Add the fixed class to the header when you reach its scroll position. Remove "fixed" when you leave the scroll position
                if (window.pageYOffset > elMaxiHeight-elMiniHeight) {
                    elMini.classList.add("visible");
                    elMini.style.top=0;//top
                    elMini.style.paddingTop = currentTopOffset+"px";  
                    elMini.style.paddingLeft = currentLeftOffset+"px";    			
                } else {
                    elMini.classList.remove("visible");
                    elMini.style.top = (-elMiniHeight-currentTopOffset)+"px";
                }        		
            };
            
            Vue.minifyComputeOffset = function(offset, offsetElSelector, defaultOffset, direction) {
                var currentOffset = defaultOffset;
                if(offset) {
                    currentOffset = offset;
                } else if(offsetElSelector) {
                    var offsetElement = document.querySelector(offsetElSelector);
                    if(direction === 'LEFT') {
                        currentOffset = offsetElement.getBoundingClientRect().width+offsetElement.getBoundingClientRect().x;
                    } else if(direction === 'TOP') {
                        currentOffset = offsetElement.getBoundingClientRect().height+offsetElement.getBoundingClientRect().y;
                    }
                }
                return currentOffset;
            }
            window.addEventListener('scroll', Vue.minifyHandler)
            window.addEventListener('resize', Quasar.utils.throttle(Vue.minifyHandler,50))
        },
        componentUpdated : function() {
            const interval = 50;
            const maxDelay = 1000;
            for(var delay = interval ; delay < maxDelay ; delay += delay) {
                setTimeout(Vue.minifyHandler,delay);
            }
        },
        unbind: function(elMaxi) {
            window.removeEventListener('scroll');
            window.removeEventListener('resize');
            
            for(var i=0 ; i<elMaxi.childNodes.length; i++) {
                var elChild = elMaxi.childNodes[i];
                if(elChild.classList) {
                    elChild.classList.remove("not-mini")
                }
            }
        },
    }