class File
        constructor: (data) ->
                { @isDirectory, @isSymLink, @replication, @path, @owner, @group, @permission, @modification_time } = data
                @selected = false
                [_, _...,@name] = @path.split "/" 

        render: () ->
                result = '<tr>'
                result += '<td><input type="checkbox" ng-model="seleted"></td>'
                if @isDirectory
                        result += '<td><i class="fa fa-folder"></i></td>'
                else if @isSymlink
                        result += '<td><i class="fa fa-external-link"></i></td>'
                else
                        result += '<td><i class="fa fa-file"></i></td>'
                result += "<td>#{@name}</td>"
                result += "<td>#{@replication}</td>"
                result += '<td>15 Kb</td>'
                result += "<td>#{@owner}</td>"
                result += "<td>#{@group}</td>"
                result += "<td>#{@permission}</td>"
                result += "<td>#{new Date(@modification_time)}</td>"
                result += "</tr>"
                return result
                
        
        
@DfsCtrl = ($scope, $http, $location) ->
        $scope.fs = []
        $scope.init = () ->
                if not $location.path()
                        $.ajax  "/dfs/homedir/get",
                                type: "GET"
                                async: false
                                success: (result) ->
                                        $location.path(result.homedir)
                                        $scope.listdir()


        $scope.listdir = () ->
                if not $location.path()
                        $scope.init()
                $http.get("/dfs/listdir/" + $location.path()).success(
                        (data) ->
                                $scope.fs = (new File(f) for f in data)
                )


        $scope.renderFs = () ->
                result = """
                <tr>
                 <td></td>
                 <td><i class="fa fa-folder"></i></td>
                 <td>0</td>
                 <td>.</td>
                 <td></td>
                 <td>test</td>
                 <td>test</td>
                 <td>drwxr-xr-x</td>
                 <td>September 11, 2013 05:57 pm</td>
                </tr>
                <tr>
                 <td></td>
                 <td><i class="fa fa-folder"></i><i class="icon-level-up"></i> </td>
                 <td>0</td>
                 <td>..</td>
                 <td></td>
                 <td>test</td>
                 <td>test</td>
                 <td>drwxr-xr-x</td>
                 <td>September 11, 2013 05:57 pm</td>
                </tr>
                """
                console.log $scope.fs
                result = ""
                for f in $scope.fs
                        result += f.render()
                return result

                        
                        
                
app = angular.module("DfsApp", [])
app.controller("DfsCtrl", @DfsCtrl)

