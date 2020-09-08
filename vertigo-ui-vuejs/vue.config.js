module.exports = {
    configureWebpack: {
    	output: {
    	  library: 'VertigoUi', // Add this line to expose the library in the devServer
    	  libraryTarget: 'umd'
    	},
	    devServer: {
	      headers: { 'Access-Control-Allow-Origin': '*' }
	    },
        externals: {
            quasar: {
                commonjs: 'quasar',
                commonjs2: 'quasar',
                root: 'Quasar'
            },
            ol: {
                commonjs: 'ol',
                commonjs2: 'ol',
                root: 'ol'
            },
            vue: {
                commonjs: 'vue',
                commonjs2: 'vue',
                root: 'Vue'
            }
        }
    }
}