/**
 * Directive scroll-spy
 * @param offset int : Offset from top screen, used for static header
 * 
 * Usage : Just put this directive on your nav container.
 * This directive looks for all 'A' tags, extract their href and link them to page's container with same id
 * 
 * Example :  
 * <nav v-scroll-spy="{offset:50}">
 *   <q-item tag="a" href="#first">First section label</q-item>			        
 *   <q-item tag="a" href="#second">Second section label</q-item>
 * </nav>
 * 
 * <div id="first">
 *	  <h1>First</h1>
 *	  <!-- content -->
 * </div>
 * <div id="second">
 *	  <h1>Second</h1>
 *	  <!-- content -->
 * </div>
 */
Vue.directive('scroll-spy', {
        bind: function(elNav, args) {
        	const debugMode = args.value.debug?args.value.debug:false;
        	
        	const offset = args.value.offset?args.value.offset:0;
        	const padding = args.value.padding?args.value.padding:24;
        	const scanner = args.value.scanner?args.value.scanner:offset+30; //scanner is 30px bottom of offset, must be smaller than the smallest first element
        	const elAs = elNav.querySelectorAll('a')
        	elAs[0].classList.add("active") //first active
        	const scrollContainer = Quasar.utils.scroll.getScrollTarget(document.querySelector(elAs[0].hash))
    		
        	if(debugMode) {
	        	const scannerLine1 = document.createElement("HR");  
	        	scannerLine1.style.position='absolute';
	        	scannerLine1.style.top=scanner+'px';
	        	scannerLine1.style.border='none';
	        	scannerLine1.style.borderTop='red solid 1px';
	        	scannerLine1.style.width='100%';
	        	scannerLine1.style.zIndex='10000';
	        	document.querySelector('body').appendChild(scannerLine1);
	        }
        	
        	Vue.scrollSpyHandler = function(scroll) {
        		// Add the fixed class to the header when you reach its scroll position. Remove "fixed" when you leave the scroll position
        		if (window.pageYOffset > offset) {
        			elNav.style.top = offset+padding+"px";
        			//when fixed, we must set a valid width, for that we use parent width
        			elNav.style.width = elNav.parentElement.getBoundingClientRect().width+"px";
        			elNav.classList.add("fixed");
        		} else {
        			elNav.classList.remove("fixed");
        			elNav.style.top = null;
        			elNav.style.width = null;
        		}
        		
        		//We compute breakpoints
        		var scrollPosition = Quasar.utils.scroll.getScrollPosition(scrollContainer)
        		var scrollBreakpoints = Vue.computeBreakPoints(scrollPosition);
        		//We looks between which breakpoints we are
        		for(var i = 0 ; i < elAs.length; i++) {
        			if(scrollBreakpoints[i] <= scrollPosition && (i >= elAs.length-1 || scrollPosition < scrollBreakpoints[i+1])) {
        				elAs[i].classList.add("active")
				  } else {
					  	elAs[i].classList.remove("active");
				  }
			    }
        	};
        	
        	Vue.scrollTo = function(event){
        		event.preventDefault();
        		const elScrollId = event.target.hash;
        		const elScroll = document.querySelector(elScrollId)
                var toScroll = Quasar.utils.scroll.getScrollPosition(scrollContainer)+elScroll.getBoundingClientRect().top-scanner
                
        		var scrollPosition = Quasar.utils.scroll.getScrollPosition(scrollContainer)
        		var scrollBreakpoints = Vue.computeBreakPoints(scrollPosition);
        		for(var i = 0 ; i < elAs.length; i++) {
        			if(elAs[i].hash == elScrollId) {
        				toScroll = scrollBreakpoints[i];
        				break;
        			}
			    }
                var duration = 200
                Quasar.utils.scroll.setScrollPosition(scrollContainer,toScroll, duration)
            };
        	
        	Vue.computeBreakPoints = function(scrollPosition){
        		var blockHeight = []
        		for(var i = 0 ; i < elAs.length; i++) {
        			const elScrollId = elAs[i].hash;
				    const elScroll = document.querySelector(elScrollId)
				    if(elScroll) {
				    	blockHeight.push(scrollPosition+elScroll.getBoundingClientRect().top);
				    	console.log(i+'  top: '+blockHeight[i] )
					} else {
						console.warn('ScrollSpy element '+elScrollId+' not found')
					}
			    }
        		
        		const windowHeight = (window.innerHeight || document.documentElement.clientHeight); /** visible height */
        		const scrollHeight = Quasar.utils.scroll.getScrollHeight(scrollContainer) /** height of scrollable element */ 
        		const scrollMax = scrollHeight - windowHeight /** Maximum possible scroll */  
        		const scrollStart = scrollMax - windowHeight + scanner; /** Start linear move at this scroll position */
        		const blockHeightDelta = blockHeight[blockHeight.length-1] - scanner - scrollStart //block position linear regression "from" length
        		const scrollDelta = windowHeight - scanner //scroll linear regression "to" length
				
        		var scrollBreakpoints = []
        		scrollBreakpoints.push(0)
        		
        		for(var i = 1 ; i < elAs.length; i++) {
        			if(blockHeight[i] > scrollStart) {
        				const blockScanFromStart = blockHeight[i] - scrollStart - scanner
        				scrollBreakpoints[i] = scrollStart + (blockScanFromStart/blockHeightDelta) * scrollDelta
        			} else {
        				scrollBreakpoints[i] = blockHeight[i] - scanner;
        			}
        			scrollBreakpoints[i] = Math.round(scrollBreakpoints[i])
        		}
        		if(debugMode) {
        			scannerLine1.style.top=scrollBreakpoints[scrollBreakpoints.length-1]+'px';
        		}
        		return scrollBreakpoints;
            };
            
            elNav.classList.add("scroll-spy-nav");
            for(var i = 0 ; i < elAs.length; i++) {
    			elAs[i].addEventListener('click', Vue.scrollTo);
    		}		    
        	window.addEventListener('scroll', Vue.scrollSpyHandler)
        	window.addEventListener('resize', Quasar.utils.throttle(Vue.scrollSpyHandler,50))
        },
        unbind: function(elNav) {
        	elNav.classList.remove("scroll-spy-nav");
        	window.removeEventListener('scroll')
        	window.removeEventListener('resize')
        	const elAs = elNav.querySelectorAll('a')
    		for(var i = 0 ; i < elAs.length; i++) {
    			elAs.removeEventListener('click')		    
    		}
        },
    });
