# Hukoomi Portal Revamp - HTML STORE

#### HTML Updates

Please deliver the HTML in a separate branch and raise the PR to development branch assigning @dhiraj-champawat for the review.

Also provide the description of the changes in the PR description.

Below are the commands for reference.

1. Clone the repo content to local directory. (One time activity)
    - `git clone --recursive https://github.com/hayagreeva/Hukoomi-Revamp.git`
    
2. Make sure the development Branch is pull to the latest version. 
    - `git checkout development`
    - `git pull`
    
3. Create a new branch for the HTML delievery
	- `git checkout -b 'homepage-html-v1'`
	
4. Copy the HTML files to `html-files` directory on the project Root.

5. Add the recently added/changed files using the below command. (Execute from Project Root folder).
	- `git add html-files`
    
6. Confirm that all the necessary files are added in the git commit using the below command.
    - `git status`
    
7. Commit the changes to the remote repository with the commit message highlighting the changes done.
    - `git commit -m "Added Homepage HTML changes"`
    
8. Push the changes to your Branch
    - `git push --set-upstream origin <YOUR_BRANCH_NAME>`

9. From the GitHub UI, browse and raise a `Pull Request` to merge your branch into `development`.

10. Provide the necessary details on the changes done in the PR Description.

11. Select @dhiraj-champawat as Reviewer.