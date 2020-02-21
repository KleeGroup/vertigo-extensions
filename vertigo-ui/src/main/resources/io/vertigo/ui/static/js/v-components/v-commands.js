Vue.component('v-commands', {
	template : `
	<div>
		<q-select v-if="!isCommandCommited" :placeholder="$q.lang.vui.commands.globalPlaceholder" outlined bg-color="white" dense ref="commandInput" autofocus dropdown-icon="search" @blur="reset"
				use-input input-debounce="300" hide-selected  v-on:keydown.native="commitCommand"
				:options="commandAutocompleteOptions" @filter="searchCommands" @input="selectCommand">
			<span v-if="text !== '' && selectedCommand.commandName && selectedCommand.commandName.startsWith(text)" style="z-index= -1; line-height: 40px; opacity:0.5; position:fixed;">{{selectedCommand.commandName}}</span>
		</q-select>
		<div v-else class="row col-12 justify-between bg-white round-borders overflow-hidden shadow-2 text-black" v-on:keyup.enter.native="executeCommand">
			<div class="bg-grey-4 text-center vertical-middle text-bold q-px-md" style="line-height: 40px;">{{selectedCommand.commandName}}</div>
			<div v-if="!isExecuted" class="row col items-center q-py-xs">
				<template v-if="selectedCommand.commandParams && selectedCommand.commandParams.length > 0" v-for="(param, index) in selectedCommand.commandParams" >
					<template v-if="param.paramType.rawType === 'io.vertigo.commons.command.GenericUID'">
						<q-select class="col q-px-xs" use-chips bg-color="white" dense borderless use-input input-debounce="300" :value="getParamValue(index)" :options="paramsAutocompleteOptions[index]" :autofocus="index === 0" dropdown-icon="search" 
							  v-on:keydown.delete.native="function(event) {backIfNeeded(event, index === 0)}" v-on:keyup.esc.native="function(event) {backIfNeeded(event, index === 0)}"   
							  @filter="function(val, update, abort) { autocompleteParam(param, index, val, update, abort);}" v-on:input="function (newValue) { selectParam(newValue, index)}" style="height:32px;">
						</q-select>
					</template>
					<template v-else>
						<q-input class="col q-px-xs" color="secondary" borderless v-model="commandParamsValues[index].value" @keydown.delete="function(event) {backIfNeeded(event, index === 0)}" @keyup.esc="function(event) {backIfNeeded(event, index === 0)}" :autofocus="index === 0" @keyup.enter="handleEnter(index)" dense style="height:32px;">
						</q-input>
					</template>
					<q-separator vertical></q-separator>
				</template>
				<div class="col" v-else>{{$q.lang.vui.commands.executeCommand}}</div>
				<q-btn @click="executeCommand" flat icon="play_arrow" size="sm" round></q-btn>
			</div>
			<div v-else class="row col items-center" >
				<div class="col shadow-2 bg-secondary text-white q-px-md" style="line-height: 40px;">{{commandResult.display}}</div>
				<q-btn v-if="commandResult.targetUrl" type="a" :href="baseUrl + commandResult.targetUrl" flat  >{{$q.lang.vui.commands.linkLabel}}</q-btn>
				<q-btn @click="reset" flat icon="cancel" size="sm" round></q-btn>
			</div>
		</div>
	</div>
	`
	,
	data: function() {
		return {
			text: "",
			commandParamsValues: [],
			commands : [],
			commandAutocompleteOptions : [],
			isCommandCommited: false,
			selectedCommand: {},
			isExecuted: false,
			commandResult : {},
			paramsAutocompleteOptions: []
		}
	},
	props : {
		baseUrl : { type: String, 'default': '/' },
	},
	methods : {
		searchCommands : function(val, update, abort) {
			this.$data.text = val;
			this.$data.selectedCommand = {};
			if (val.length < 1) {
				abort();
				return;
			}
			this.$http.post(this.baseUrl+'api/vertigo/commands/_search', {prefix: val} )
		        .then( function (response) { //Ok
		        	this.$data.commands = response.body;
		        	update(function() {
			        	this.$data.commandAutocompleteOptions = this.$data.commands.map(function(command) {
							 return {
								 label: command.commandName,
								 sublabel: command.descpription,
								 value: command.commandName,
								 command : command
							 }
						});
					}.bind(this));
		        	if (this.$data.commands.length > 0) {
		        		this.chooseCommand(this.$data.commands[0], false);
		        	}
				});
			
		},
		selectCommand : function (selection) {
			this.chooseCommand(selection.command, true);
		},
		chooseCommand: function (command, commitCommand) {
			this.$data.selectedCommand = command;
			if (this.$data.selectedCommand.commandParams) {
				// if we have params
				this.$data.commandParamsValues = this.$data.selectedCommand.commandParams.map(function(param)  {
					// we prepare params
					return {
						value:""
					};	
				});
			} else {
				this.$data.commandParamsValues = [];
			}
			this.$data.isCommandCommited = commitCommand;
		},
		commitCommand : function(keyEvent) {
			if (this.$data.selectedCommand && this.$data.selectedCommand.commandName) {
				switch(keyEvent.keyCode) {
					case 9:
					case 13:
						this.$data.isCommandCommited = true;
						keyEvent.preventDefault();
				}
			}
		},
		executeCommand : function() {
			if (this.$data.commandParamsValues.every(function(paramValue){ return paramValue;})) {
				var actualParams = this.$data.commandParamsValues.map(function (param) {
					return param.value;
				});
				
				this.$http.post(this.baseUrl+'api/vertigo/commands/_execute', {command: this.$data.selectedCommand.commandName, params: actualParams} )
			        .then( function (response) { //Ok
			        	this.$data.isExecuted = true;
			        	this.$data.commandResult = response.body;
					});
			} else {
				return false;
			}
			
		},
		handleEnter: function (index, event) {
			if (index === this.$data.selectedCommand.commandParams.length - 1 && this.$data.commandParamsValues[index].value) {
				this.executeCommand();
			}
			// otherwise nothing particular
		},
		autocompleteParam : function(param, index, val, update, abort) {
			if (val.length < 1) {
				abort();
				return;
			}
			this.$http.get(this.baseUrl+'api/vertigo/commands/params/_autocomplete', {params : {terms: val, entityClass: param.paramType.actualTypeArguments[0]}})
				.then( function (response) {
					update(function() {
						var newOptions = this.$data.paramsAutocompleteOptions.slice();
						newOptions[index] = response.body.map(function(element) {
							return {
								label: element.label,
								value: element.urn,
							}
						});
						this.$data.paramsAutocompleteOptions = newOptions;
					}.bind(this));
				});
			
		},
		selectParam : function (selection, index) {
			var newParams = this.$data.commandParamsValues.slice();
			newParams[index] = selection;
			this.$data.commandParamsValues = newParams;
			
		},
		getParamValue(index) {
			var actualValue = this.$data.commandParamsValues[index];
			if (actualValue && actualValue.value) {
				return actualValue;
			}
		},
		backIfNeeded: function (index, isFirst) {
			if (isFirst && !this.$data.commandParamsValues[0].value) {
				this.back();
			}
			// otherwise nothing particular
		},
		back: function () {
			this.$data.commandParamsValues = [];
			this.$data.commands = [];
			this.$data.isCommandCommited = false;
			this.$data.selectedCommand = {};
			this.$data.isExecuted = false;
			this.$data.commandResult = {};
			this.$data.paramsAutocompleteOptions = [];
			this.$nextTick(function () {
				this.$refs.commandInput.updateInputValue(this.$data.text);
			});
			
		},
		reset: function() {
			this.back();
			this.$data.text = "";
		}
	} 
})
