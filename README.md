# Hukoomi Portal Revamp

This Repository will be used for Hukoomi Portal Revamp code.

The Branches we map will be based on the below structure.

Git Branch | TS/LS Environment
-- | --
development | Dev TS/LS server
testing | Testing TS/LS server
master | Production TS/LS server

Please note that the deployment as of now is manual, developer needs to deploy the jar files manually after each mvn compile.

# Development Workflow

Below are the Git commands often used while making changes to Git Repo.

1. Clone the repo content to local directory
    - `git clone --recursive https://github.com/mannaiTeamsite/mannai.git`
    
2. Make Branch for individual stories/tasks 
    - `git branch <new-branch-name|story-name>`
    
3. Traverse to the new branch to do changes as per the below directory structure
	
	Area of Functionality | Source Code Directory
	-- | --
	Teamsite Functions and Workflows Specific External Java class | teamsite-core/src
	Livesite Components External Java class | livesite-core/src
	Callout JSP files and CGI Files | teamsite-core/src/iw-cc	
	Inline Perl files and CGI Files | teamsite-core/src/httpd/iw-bin
	DCT specific JS Files | teamsite-core/src/httpd/iw
	
	- `git checkout <new-branch-name|story-name>`
    
4. After doing changes to branch files, do maven build to check for code validation
    - `mvn clean install`
    
5. To confirm we are on the new branch & to list the changed files
    - `git status`
    
6. Add/Remove the necessary files to be commited
    - `git add|rm <file>`
  
7. Commit the changes to the remote repository with the commit message mentioning the changes done.
    - `git commit -m "added my github changes"`
    
8. Push the changes to your Branch
    - `git push`

9. From the GitHub UI, browse and raise a pull request to merge your branch into `development`. Provide the necessary details on the changes done in the PR. Select @dhiraj-champawat / @VinayaganS as Reviewer.
